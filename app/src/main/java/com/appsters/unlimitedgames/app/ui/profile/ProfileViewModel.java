package com.appsters.unlimitedgames.app.ui.profile;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.appsters.unlimitedgames.app.data.model.Score;
import com.appsters.unlimitedgames.app.data.model.User;
import com.appsters.unlimitedgames.app.data.repository.LeaderboardRepository;
import com.appsters.unlimitedgames.app.data.repository.UserRepository;
import com.appsters.unlimitedgames.app.util.GameType;
import com.appsters.unlimitedgames.app.util.Privacy;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap; // Import HashMap
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final UserRepository userRepository;
    private final LeaderboardRepository leaderboardRepository;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<List<Score>> userScores = new MutableLiveData<>();
    private final MutableLiveData<Double> averageRank = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutComplete = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> imageUploadSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>(false);

    public ProfileViewModel() {
        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        leaderboardRepository = new LeaderboardRepository();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<Score>> getUserScores() {
        return userScores;
    }

    public LiveData<Double> getAverageRank() {
        return averageRank;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLogoutComplete() {
        return logoutComplete;
    }

    public LiveData<Boolean> getImageUploadSuccess() {
        return imageUploadSuccess;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }

    public void resetFlags() {
        updateSuccess.setValue(false);
        deleteSuccess.setValue(false);
        imageUploadSuccess.setValue(false);
        logoutComplete.setValue(false);
        errorMessage.setValue(null);
    }

    public void loadCurrentUser(Context context) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            errorMessage.setValue("No user logged in");
            return;
        }

        isLoading.setValue(true);
        userRepository.getUser(firebaseUser.getUid(), task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                currentUser.setValue(task.getResult());
                loadUserScores(firebaseUser.getUid());
            } else {
                errorMessage.setValue("Failed to load profile: " + task.getException().getMessage());
            }
        });
    }

    private void loadUserScores(String userId) {
        leaderboardRepository.getUserScores(userId, (isSuccessful, scores, e) -> {
            if (isSuccessful) {
                // Now, get all global leaderboards to calculate ranks
                fetchAllLeaderboardsAndCalculateRanks(scores, userId);
            } else {
                errorMessage.setValue("Failed to load user scores.");
            }
        });
    }

    private void fetchAllLeaderboardsAndCalculateRanks(List<Score> userScores, String userId) {
        List<GameType> gameTypes = new ArrayList<>();
        for (GameType type : GameType.values()) {
            if (type != GameType.ALL) {
                gameTypes.add(type);
            }
        }

        List<List<Score>> allLeaderboards = new ArrayList<>();
        for (GameType gameType : gameTypes) {
            leaderboardRepository.getGlobalLeaderboard(gameType, 100, (isSuccessful, scores, e) -> {
                if (isSuccessful) {
                    allLeaderboards.add(scores);
                    if (allLeaderboards.size() == gameTypes.size()) {
                        // All leaderboards fetched, now calculate ranks
                        calculateRanks(userScores, allLeaderboards, userId);
                    }
                } else {
                    errorMessage.setValue("Failed to load leaderboards for rank calculation.");
                }
            });
        }
    }

    private void calculateRanks(List<Score> userScores, List<List<Score>> allLeaderboards, String userId) {
        Map<GameType, Integer> userRanks = new HashMap<>();
        int totalRank = 0;
        int gamesPlayed = 0;

        for (List<Score> leaderboard : allLeaderboards) {
            for (int i = 0; i < leaderboard.size(); i++) {
                Score score = leaderboard.get(i);
                if (score.getUserId().equals(userId)) {
                    userRanks.put(score.getGameType(), i + 1);
                    totalRank += (i + 1);
                    gamesPlayed++;
                    break;
                }
            }
        }

        if (gamesPlayed > 0) {
            averageRank.setValue((double) totalRank / gamesPlayed);
        }

        // Add rank to user scores
        for (Score score : userScores) {
            Integer rank = userRanks.get(score.getGameType());
            if (rank != null) {
                score.setRank(rank);
            }
        }
        this.userScores.setValue(userScores);
    }

    public void updatePrivacy(Privacy privacy) {
        User user = currentUser.getValue();
        if (user == null) return;

        user.setPrivacy(privacy);
        updateUser(user);
    }

    public void updateProfilePicture(String base64Image) {
        User user = currentUser.getValue();
        if (user == null) return;

        user.setProfileImageUrl(base64Image);
        imageUploadSuccess.setValue(false);
        updateUser(user);
    }

    public void updateProfile(String newUsername, String newEmail, String currentPassword,
                              String newPassword, String confirmPassword) {
        User user = currentUser.getValue();
        if (user == null) {
            errorMessage.setValue("User data not loaded");
            return;
        }

        ValidationResult validation = validateProfileUpdate(
                user, newUsername, newEmail, currentPassword, newPassword, confirmPassword
        );

        if (!validation.isValid) {
            errorMessage.setValue(validation.errorMessage);
            return;
        }

        boolean usernameChanged = !newUsername.equals(user.getUsername());
        boolean emailChanged = !newEmail.equals(user.getEmail());
        boolean passwordChanged = !TextUtils.isEmpty(newPassword);

        if (emailChanged || passwordChanged) {
            updateWithReauth(user, newUsername, newEmail, currentPassword,
                    newPassword, usernameChanged, emailChanged, passwordChanged);
        } else if (usernameChanged) {
            updateUsername(newUsername);
        } else {
            errorMessage.setValue("No changes detected");
        }
    }

    private ValidationResult validateProfileUpdate(User user, String newUsername, String newEmail,
                                                   String currentPassword, String newPassword,
                                                   String confirmPassword) {
        if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            return new ValidationResult(false, "Invalid email address");
        }

        boolean emailChanged = !newEmail.equals(user.getEmail());
        boolean passwordChanged = !TextUtils.isEmpty(newPassword);

        if ((emailChanged || passwordChanged) && currentPassword.isEmpty()) {
            return new ValidationResult(false, "Current password required to change email or password");
        }

        if (passwordChanged) {
            if (TextUtils.isEmpty(confirmPassword)) {
                return new ValidationResult(false, "Please confirm new password");
            }
            if (!newPassword.equals(confirmPassword)) {
                return new ValidationResult(false, "New passwords do not match");
            }
            if (newPassword.length() < 6) {
                return new ValidationResult(false, "Password must be at least 6 characters");
            }
            if (newPassword.equals(currentPassword)) {
                return new ValidationResult(false, "New password must be different from current password");
            }
        }

        return new ValidationResult(true, null);
    }

    private static class ValidationResult {
        boolean isValid;
        String errorMessage;

        ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }

    private void updateWithReauth(User user, String newEmail, String newUsername,
                                  String currentPassword, String newPassword,
                                  boolean usernameChanged, boolean emailChanged,
                                  boolean passwordChanged) {
        reauthenticate(user.getEmail(), currentPassword, () -> {
            FirebaseUser firebaseUser = auth.getCurrentUser();
            if (firebaseUser == null) {
                errorMessage.setValue("Authentication error");
                return;
            }

            isLoading.setValue(true);

            if (emailChanged) {
                updateEmailThenContinue(firebaseUser, user, newEmail, newPassword,
                        newUsername, passwordChanged, usernameChanged);
            } else if (passwordChanged) {
                updatePasswordThenContinue(firebaseUser, user, newUsername,
                        newPassword, usernameChanged);
            }
        });
    }

    private void updateEmailThenContinue(FirebaseUser firebaseUser, User user, String newEmail,
                                         String newPassword, String newUsername,
                                         boolean passwordChanged, boolean usernameChanged) {
        firebaseUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.setEmail(newEmail);

                if (passwordChanged) {
                    updatePasswordThenContinue(firebaseUser, user, newUsername,
                            newPassword, usernameChanged);
                } else {
                    finalizeProfileUpdate(user, newUsername, usernameChanged);
                }
            } else {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update email: " + task.getException().getMessage());
            }
        });
    }

    private void updatePasswordThenContinue(FirebaseUser firebaseUser, User user,
                                            String newUsername, String newPassword,
                                            boolean usernameChanged) {
        firebaseUser.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                finalizeProfileUpdate(user, newUsername, usernameChanged);
            } else {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update password: " + task.getException().getMessage());
            }
        });
    }

    private void finalizeProfileUpdate(User user, String newUsername, boolean usernameChanged) {
        if (usernameChanged) {
            user.setUsername(newUsername);
        }
        updateUser(user);
    }

    public void updateUsername(String newUsername) {
        User user = currentUser.getValue();
        if (user == null) return;

        user.setUsername(newUsername);
        updateUser(user);
    }

    public void updateUserEmail(String newEmail, String password) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        User user = currentUser.getValue();
        if (firebaseUser == null || user == null) return;
        isLoading.setValue(true);
        reauthenticate(user.getEmail(), password, () -> {
                firebaseUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.setEmail(newEmail);
                    updateUser(user);
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to update email: " + task.getException().getMessage());
                }
            });
        });
    }

    public void updatePassword(String currentPassword, String newPassword) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        User user = currentUser.getValue();
        if (firebaseUser == null || user == null) return;

        reauthenticate(user.getEmail(), currentPassword, () -> {
            isLoading.setValue(true);
            firebaseUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                isLoading.setValue(false);
                if (task.isSuccessful()) {
                    updateSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Failed to update password: " + task.getException().getMessage());
                }
            });
        });
    }

    public void deleteAccount(String password) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        User user = currentUser.getValue();
        if (firebaseUser == null || user == null) return;

        reauthenticate(user.getEmail(), password, () -> {
            isLoading.setValue(true);
            userRepository.deleteUser(user.getUserId(), task -> {
                if (task.isSuccessful()) {
                    firebaseUser.delete().addOnCompleteListener(deleteTask -> {
                        isLoading.setValue(false);
                        if (deleteTask.isSuccessful()) {
                            currentUser.setValue(null);
                            deleteSuccess.setValue(true);
                        } else {
                            errorMessage.setValue("Failed to delete Firebase Auth user: " + deleteTask.getException().getMessage());
                        }
                    });
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to delete user data: " + task.getException().getMessage());
                }
            });
        });
    }

    private void updateUser(User userToUpdate) {
        isLoading.setValue(true);
        userRepository.updateUser(userToUpdate, task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                currentUser.setValue(userToUpdate);
                updateSuccess.setValue(true);
            } else {
                if (task.getException() != null) {
                    errorMessage.setValue("Failed to update profile: " + task.getException().getMessage());
                } else {
                    errorMessage.setValue("Failed to update profile");
                }
            }
        });
    }

    private void reauthenticate(String email, String password, Runnable onReAuthSuccess) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        isLoading.setValue(true);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                onReAuthSuccess.run();
            } else {
                errorMessage.setValue("Re-authentication failed: " + task.getException().getMessage());
            }
        });
    }

    public void logout() {
        currentUser.setValue(null);
        logoutComplete.setValue(true);
    }
}

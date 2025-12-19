package com.appsters.unlimitedgames.games.poker;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.appsters.unlimitedgames.app.data.model.Score;
import com.appsters.unlimitedgames.app.data.model.User;
import com.appsters.unlimitedgames.app.data.repository.LeaderboardRepository;
import com.appsters.unlimitedgames.app.data.repository.UserRepository;
import com.appsters.unlimitedgames.app.util.GameType;
import com.appsters.unlimitedgames.games.poker.model.Card;
import com.appsters.unlimitedgames.games.poker.model.Deck;
import com.appsters.unlimitedgames.games.poker.model.HandEvaluator;
import com.appsters.unlimitedgames.games.poker.repo.RandPokerRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A lightweight Texas Hold'em single-hand experience.
 *
 * Design choices (kept simple on purpose):
 * - One betting interaction per hand (player acts, dealer responds).
 * - No folds (as requested). If a raise happens, the other side auto-calls.
 * - Dealer "skill" increases with buy-in amount (5 / 50 / 500).
 */
public class PokerViewModel extends AndroidViewModel {

    public enum Phase {
        LOBBY, IN_HAND, SHOWDOWN
    }

    public enum HandStage {
        PREFLOP, FLOP, TURN, RIVER, SHOWDOWN
    }

    private final RandPokerRepository repository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private final Random random = new Random();

    private final MutableLiveData<Integer> _coins = new MutableLiveData<>(0);
    public LiveData<Integer> coins = _coins;

    private final MutableLiveData<Boolean> _freeAvailable = new MutableLiveData<>(false);
    public LiveData<Boolean> freeAvailable = _freeAvailable;

    private final MutableLiveData<String> _freeTimerText = new MutableLiveData<>("");
    public LiveData<String> freeTimerText = _freeTimerText;

    private final MutableLiveData<Integer> _selectedBuyIn = new MutableLiveData<>(5);
    public LiveData<Integer> selectedBuyIn = _selectedBuyIn;

    private final MutableLiveData<Phase> _phase = new MutableLiveData<>(Phase.LOBBY);
    public LiveData<Phase> phase = _phase;

    private final MutableLiveData<HandStage> _handStage = new MutableLiveData<>(HandStage.PREFLOP);
    public LiveData<HandStage> handStage = _handStage;

    private final MutableLiveData<Boolean> _revealEnabled = new MutableLiveData<>(false);
    public LiveData<Boolean> revealEnabled = _revealEnabled;

    private final MutableLiveData<String> _revealButtonText = new MutableLiveData<>("Reveal Flop");
    public LiveData<String> revealButtonText = _revealButtonText;

    private final MutableLiveData<String> _status = new MutableLiveData<>("");
    public LiveData<String> status = _status;

    private final MutableLiveData<String> _playerCardsText = new MutableLiveData<>("");
    public LiveData<String> playerCardsText = _playerCardsText;

    private final MutableLiveData<String> _dealerCardsText = new MutableLiveData<>("");
    public LiveData<String> dealerCardsText = _dealerCardsText;

    private final MutableLiveData<String> _boardCardsText = new MutableLiveData<>("");
    public LiveData<String> boardCardsText = _boardCardsText;

    private final MutableLiveData<String> _potTotalText = new MutableLiveData<>("0");
    public LiveData<String> potTotalText = _potTotalText;

    private final MutableLiveData<String> _winningsText = new MutableLiveData<>("winnings: 0 ");
    public LiveData<String> winningsText = _winningsText;

    private final MutableLiveData<Boolean> _bettingEnabled = new MutableLiveData<>(false);
    public LiveData<Boolean> bettingEnabled = _bettingEnabled;

    private final MutableLiveData<Boolean> _raiseEnabled = new MutableLiveData<>(false);
    public LiveData<Boolean> raiseEnabled = _raiseEnabled;

    // Raise amount slider (additional chips to put in on a raise for the current
    // street)
    private final MutableLiveData<Integer> _raiseMax = new MutableLiveData<>(1); // absolute max raise amount
    public LiveData<Integer> raiseMax = _raiseMax;

    private final MutableLiveData<Integer> _raiseAmount = new MutableLiveData<>(1); // current chosen raise amount
    public LiveData<Integer> raiseAmount = _raiseAmount;

    private final MutableLiveData<String> _raiseAmountText = new MutableLiveData<>("Raise add: 1");
    public LiveData<String> raiseAmountText = _raiseAmountText;

    private CountDownTimer freeTimer;

    // Expose card lists for UI binding
    private final MutableLiveData<List<String>> _playerCardUrls = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> playerCardUrls = _playerCardUrls;

    private final MutableLiveData<List<String>> _dealerCardUrls = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> dealerCardUrls = _dealerCardUrls;

    private final MutableLiveData<List<String>> _boardCardUrls = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> boardCardUrls = _boardCardUrls;

    private final MutableLiveData<Boolean> _dealerCardsFaceUp = new MutableLiveData<>(false);
    public LiveData<Boolean> dealerCardsFaceUp = _dealerCardsFaceUp;

    // Current hand state
    private int buyIn = 5;
    private int multiplier = 2; // 2x, 3x, 5x

    // Betting/pot state
    private int playerContribution = 0;
    private int potTotal = 0;

    // Street progression
    private HandStage currentStreet = HandStage.PREFLOP;
    private boolean awaitingBet = true;
    private int revealedCount = 0; // 0, 3, 4, 5

    private List<Card> playerHole = new ArrayList<>();
    private List<Card> dealerHole = new ArrayList<>();
    private List<Card> board = new ArrayList<>();

    public PokerViewModel(@NonNull Application application) {
        super(application);
        repository = new RandPokerRepository(application);
        leaderboardRepository = new LeaderboardRepository(application);
        userRepository = new UserRepository();

        if (!isSignedIn()) {
            _status.setValue("Sign in to play Poker and earn coins.");
            _freeTimerText.setValue("Sign in required for free coins");
            _freeAvailable.setValue(false);
            return;
        }

        refreshCoins();
        startOrRefreshFreeCoinsTimer();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (freeTimer != null)
            freeTimer.cancel();
    }

    public void refreshCoins() {
        if (!isSignedIn()) {
            _coins.setValue(0);
            _freeAvailable.setValue(false);
            _freeTimerText.setValue("Sign in required for free coins");
            return;
        }
        repository.ensureInitialized();
        _coins.setValue(repository.getCoins());
        _freeAvailable.setValue(repository.canClaimFreeCoins());

        // If we're in a hand, keep slider/raise state consistent with refreshed coin
        // balance.
        if (_phase.getValue() == Phase.IN_HAND) {
            updateRaiseSlider();
            updateRaiseEnabled();
            updatePotAndWinnings();
        }
    }

    public void selectBuyIn(int amount) {
        _selectedBuyIn.setValue(amount);
    }

    /**
     * Called by UI slider; progress is 0..(raiseMax-1) and maps to raise amount
     * 1..raiseMax.
     */
    public void setRaiseSliderProgress(int progress) {
        Integer max = _raiseMax.getValue();
        if (max == null)
            max = 1;
        int amount = Math.max(1, Math.min(max, progress + 1));
        _raiseAmount.setValue(amount);
        _raiseAmountText.setValue("Raise add: " + amount);
        updateRaiseEnabled();
        updatePotAndWinnings();
    }

    public void claimFreeCoins() {
        boolean granted = repository.claimFreeCoins();
        if (granted) {
            refreshCoins();
            submitCoinsToLeaderboard();
            _status.setValue("Claimed +10 coins.");
        } else {
            _status.setValue("Free coins aren't ready yet.");
        }
        startOrRefreshFreeCoinsTimer();
    }

    public void startHand() {
        Integer c = _coins.getValue();
        Integer sel = _selectedBuyIn.getValue();
        if (c == null)
            c = 0;
        if (sel == null)
            sel = 5;

        if (c < sel * 2) {
            _status.setValue("You need at least " + (sel * 2) + " coins to buy in for " + sel + ".");
            return;
        }

        buyIn = sel;
        multiplier = (buyIn == 5) ? 2 : (buyIn == 50 ? 3 : 5);
        buyIn = sel;
        multiplier = (buyIn == 5) ? 2 : (buyIn == 50 ? 3 : 5);

        // Buy-in immediately goes into the pot and is deducted from your coin total.
        applyCoinDelta(-buyIn);
        playerContribution = buyIn;
        potTotal = buyIn * 2;

        currentStreet = HandStage.PREFLOP;
        awaitingBet = true;
        revealedCount = 0;
        Deck deck = new Deck();
        deck.shuffle(random);

        playerHole = new ArrayList<>();
        dealerHole = new ArrayList<>();
        board = new ArrayList<>();

        playerHole.add(deck.draw());
        dealerHole.add(deck.draw());
        playerHole.add(deck.draw());
        dealerHole.add(deck.draw());

        // Community cards (flop/turn/river) - we deal all now but reveal as one string
        // for simplicity
        for (int i = 0; i < 5; i++)
            board.add(deck.draw());

        updateUiCards(); // New method to update card lists

        _dealerCardsFaceUp.setValue(false); // Hide dealer cards initially

        _playerCardsText.setValue(cardListToString(playerHole));
        _dealerCardsText.setValue("🂠 🂠"); // hidden until showdown
        _handStage.setValue(HandStage.PREFLOP);
        _revealEnabled.setValue(false);
        _revealButtonText.setValue("Reveal Flop");
        _boardCardsText.setValue(boardToStringRevealed(0));
        _bettingEnabled.setValue(true);
        updatePotAndWinnings();
        updateRaiseSlider();
        updateRaiseEnabled();
        _status.setValue("New hand (pre-flop). Your move: check or raise.");
        _phase.setValue(Phase.IN_HAND);
    }

    public void playerCheck() {
        if (_phase.getValue() != Phase.IN_HAND)
            return;
        if (!Boolean.TRUE.equals(_bettingEnabled.getValue()))
            return;

        // Dealer responds to a check on this street.
        int dealerRaise = dealerDecideRaiseAmount(/* playerRaised= */false, /* playerAggression= */0);

        // Keep it safe: dealer cannot raise more than the player can currently afford
        // to call.
        Integer c = _coins.getValue();
        if (c == null)
            c = repository.getCoins();
        int remaining = c;
        if (dealerRaise > remaining)
            dealerRaise = 0;

        if (dealerRaise > 0) {
            // Player auto-calls (no fold). Both contribute dealerRaise.
            applyCoinDelta(-dealerRaise); // you pay to call immediately
            potTotal += dealerRaise * 2;
            playerContribution += dealerRaise;
            _status.setValue(streetName(currentStreet) + ": You check. Dealer raises +" + dealerRaise + ". You call.");
        } else {
            _status.setValue(streetName(currentStreet) + ": You check. Dealer checks.");
        }

        finishBettingForStreet(/* playerRaised= */false);
    }

    public void playerRaise() {
        if (_phase.getValue() != Phase.IN_HAND)
            return;
        if (!Boolean.TRUE.equals(_bettingEnabled.getValue()))
            return;

        Integer ra = _raiseAmount.getValue();
        int raise = (ra == null ? buyIn : ra);

        // Ensure player has enough coins to cover this street's raise.
        Integer c = _coins.getValue();
        if (c == null)
            c = repository.getCoins();
        int remaining = c;
        if (remaining < raise) {
            _status.setValue("Not enough coins to raise right now.");
            updateRaiseEnabled();
            return;
        }

        // Player raises; dealer auto-calls (no fold). Both contribute 'raise'.
        applyCoinDelta(-raise); // you pay the raise immediately
        potTotal += raise * 2;
        playerContribution += raise;

        int dealerRaise = dealerDecideRaiseAmount(/* playerRaised= */true, /* playerAggression= */1);
        if (dealerRaise > 0) {
            // Dealer raises once; player auto-calls.
            // (Still cap to one extra raise per street to keep bankroll reasonable.)
            if (repository.getCoins() < dealerRaise) {
                // Can't safely cover the extra; dealer just calls instead.
                dealerRaise = 0;
            } else {
                applyCoinDelta(-dealerRaise); // you pay to call the dealer's raise
                potTotal += dealerRaise * 2;
                playerContribution += dealerRaise;
            }
        }

        if (dealerRaise > 0) {
            _status.setValue(streetName(currentStreet) + ": You raise +" + raise + ". Dealer raises +" + dealerRaise
                    + ". You call.");
        } else {
            _status.setValue(streetName(currentStreet) + ": You raise +" + raise + ". Dealer calls.");
        }

        finishBettingForStreet(/* playerRaised= */true);
    }

    private String streetName(HandStage stage) {
        switch (stage) {
            case PREFLOP:
                return "Pre-flop";
            case FLOP:
                return "Flop";
            case TURN:
                return "Turn";
            case RIVER:
                return "River";
            default:
                return "";
        }
    }

    private String nextRevealButtonText() {
        if (revealedCount == 0)
            return "Reveal Flop";
        if (revealedCount == 3)
            return "Reveal Turn";
        if (revealedCount == 4)
            return "Reveal River";
        return "Showdown";
    }

    private void updatePotAndWinnings() {
        // We always keep the pot balanced (dealer auto-calls), so the player's "win
        // payout"
        // can be computed from the total pot using the buy-in multiplier.
        int payoutIfWin = (potTotal * multiplier) / 2; // matches (playerContribution * multiplier)
        _potTotalText.setValue(String.valueOf(payoutIfWin));

        int netIfWin = Math.max(0, payoutIfWin - playerContribution);
        _winningsText.setValue("winnings: " + netIfWin + " (Net Profit)");
    }

    private void updateRaiseSlider() {
        // Slider only matters when actively betting on a street.
        boolean enabled = Boolean.TRUE.equals(_bettingEnabled.getValue());
        if (!enabled) {
            _raiseMax.setValue(1);
            _raiseAmount.setValue(1);
            _raiseAmountText.setValue("Raise add: 1");
            return;
        }

        Integer c = _coins.getValue();
        if (c == null)
            c = repository.getCoins();
        int remaining = Math.max(0, c);

        // Absolute max: you can never raise more than you can currently afford.
        int maxRaise = Math.max(1, remaining);

        _raiseMax.setValue(maxRaise);

        Integer current = _raiseAmount.getValue();
        if (current == null)
            current = 1;

        // Default the slider toward buyIn-sized raises when possible.
        if (current <= 1 && maxRaise >= buyIn)
            current = buyIn;

        int clamped = Math.max(1, Math.min(maxRaise, current));
        _raiseAmount.setValue(clamped);
        _raiseAmountText.setValue("Raise add: " + clamped);
    }

    private void updateRaiseEnabled() {
        boolean enabled = Boolean.TRUE.equals(_bettingEnabled.getValue());
        if (!enabled) {
            _raiseEnabled.setValue(false);
            return;
        }
        Integer c = _coins.getValue();
        if (c == null)
            c = repository.getCoins();
        int remaining = c;
        _raiseEnabled.setValue(remaining >= 1);
    }

    private void finishBettingForStreet(boolean playerRaised) {
        _bettingEnabled.setValue(false);
        updateRaiseSlider();
        updateRaiseEnabled();
        updatePotAndWinnings();

        if (currentStreet == HandStage.RIVER) {
            _revealEnabled.setValue(false);
            showdownAndPayout();
            return;
        }

        _revealEnabled.setValue(true);
        _revealButtonText.setValue(nextRevealButtonText());

        String prompt;
        if (revealedCount == 0)
            prompt = " Tap to reveal the flop.";
        else if (revealedCount == 3)
            prompt = " Tap to reveal the turn.";
        else
            prompt = " Tap to reveal the river.";

        String s = _status.getValue();
        if (s == null)
            s = "";
        _status.setValue(s + prompt);
    }

    private void showdownAndPayout() {
        _phase.setValue(Phase.SHOWDOWN);

        List<Card> playerSeven = new ArrayList<>();
        playerSeven.addAll(playerHole);
        playerSeven.addAll(board);

        List<Card> dealerSeven = new ArrayList<>();
        dealerSeven.addAll(dealerHole);
        dealerSeven.addAll(board);

        HandEvaluator.Result p = HandEvaluator.bestOfSeven(playerSeven);
        HandEvaluator.Result d = HandEvaluator.bestOfSeven(dealerSeven);

        _dealerCardsFaceUp.setValue(true); // Reveal dealer cards
        updateUiCards();

        _dealerCardsText.setValue(cardListToString(dealerHole));

        int delta;
        String resultText;

        // Coins are deducted as you bet (buy-in + calls/raises). At showdown we only
        // ADD winnings/refunds.
        if (p.value > d.value) {
            int payoutIfWin = (potTotal * multiplier) / 2; // equals (playerContribution * multiplier) since pot is
                                                           // balanced
            delta = payoutIfWin;
            resultText = "You win! (" + p.categoryName + " beats " + d.categoryName + ") +" + delta;
        } else if (p.value < d.value) {
            delta = 0;
            resultText = "Dealer wins. (" + d.categoryName + " beats " + p.categoryName + ") +" + delta;
        } else {
            // Tie: return your contribution (no multiplier on ties)
            delta = playerContribution;
            resultText = "Tie. (" + p.categoryName + ") +" + delta;
        }

        applyCoinDelta(delta);
        _status.setValue(resultText + " coins. Tap Start Hand to play again.");
    }

    private void applyCoinDelta(int delta) {
        int current = repository.getCoins();
        int next = Math.max(0, current + delta);
        repository.setCoins(next);
        _coins.setValue(next);
        submitCoinsToLeaderboard();
        updateRaiseEnabled();
    }

    private void submitCoinsToLeaderboard() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null)
            return;

        int score = repository.getCoins();

        userRepository.getUser(userId, userTask -> {
            if (userTask.isSuccessful()) {
                User user = userTask.getResult();
                String username = (user != null && user.getUsername() != null) ? user.getUsername() : "Unknown";
                Score scoreObject = new Score(null, userId, username, GameType.POKER, score);
                leaderboardRepository.submitScore(scoreObject, (isSuccessful, result, e) -> {
                    // no-op
                });
            }
        });
    }

    /**
     * Dealer logic:
     * - Buy-in 5: random / sloppy.
     * - Buy-in 50: uses its hole cards strength.
     * - Buy-in 500: uses hole cards + player aggression.
     */
    private int dealerDecideRaiseAmount(boolean playerRaised, int playerAggression) {
        // raise amount is always buyIn (keeps math + UX simple)
        int raiseAmount = buyIn;

        if (buyIn == 5) {
            // 35% chance to raise regardless
            return random.nextInt(100) < 35 ? raiseAmount : 0;
        }

        // Evaluate rough preflop strength from dealer hole cards.
        double strength = preflopStrength(dealerHole);

        if (buyIn == 50) {
            // Raise if decent
            return (strength >= 0.58) ? raiseAmount : 0;
        }

        // buyIn == 500: incorporate player aggression
        if (playerRaised) {
            // Player showed aggression; require stronger hand to raise back
            if (strength >= 0.70)
                return raiseAmount;
            return 0;
        } else {
            // Player checked; dealer can press with moderate hands
            return (strength >= 0.55) ? raiseAmount : 0;
        }
    }

    /**
     * Very simple preflop strength heuristic [0..1].
     */
    private double preflopStrength(List<Card> hole) {
        if (hole == null || hole.size() != 2)
            return 0.0;
        Card a = hole.get(0);
        Card b = hole.get(1);

        int high = Math.max(a.rank, b.rank);
        int low = Math.min(a.rank, b.rank);
        boolean pair = a.rank == b.rank;
        boolean suited = a.suit == b.suit;
        int gap = high - low;

        double s = 0.0;

        // Base from high card
        s += (high - 2) / 12.0 * 0.45; // up to ~0.45

        if (pair)
            s += 0.40;
        if (suited)
            s += 0.07;
        if (gap == 1)
            s += 0.05; // connectors
        if (gap == 0)
            s += 0.08; // already counted as pair, but add a bit
        if (high >= 13 && low >= 10)
            s += 0.10; // broadway-ish

        // Clamp
        if (s < 0)
            s = 0;
        if (s > 1)
            s = 1;
        return s;
    }

    private String cardListToString(List<Card> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).toString());
            if (i != cards.size() - 1)
                sb.append("  ");
        }
        return sb.toString();
    }

    private String boardToStringRevealed(int revealedCount) {
        // Show revealed community cards, hide the rest.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < revealedCount && i < board.size()) {
                sb.append(board.get(i).toString());
            } else {
                sb.append("??");
            }
            if (i != 4)
                sb.append("  ");
        }
        return sb.toString();
    }

    private void updateUiCards() {
        // Player cards (always visible)
        List<String> pUrls = new ArrayList<>();
        for (Card c : playerHole)
            pUrls.add(c.getImageUrl());
        _playerCardUrls.setValue(pUrls);

        // Dealer cards (hidden unless faceUp)
        List<String> dUrls = new ArrayList<>();
        for (Card c : dealerHole)
            dUrls.add(c.getImageUrl());
        _dealerCardUrls.setValue(dUrls); // View will handle faceDown logic via separate LiveData

        // Board cards (revealed based on count)
        List<String> bUrls = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (i < revealedCount && i < board.size()) {
                bUrls.add(board.get(i).getImageUrl());
            } else {
                bUrls.add(""); // Empty or null indicates not revealed/placeholder
            }
        }
        _boardCardUrls.setValue(bUrls);
    }

    public void revealNext() {
        Phase p = _phase.getValue();
        if (p == null || p != Phase.IN_HAND)
            return;
        if (!Boolean.TRUE.equals(_revealEnabled.getValue()))
            return;

        // Reveal next set of community cards, then allow betting on that street.
        if (revealedCount == 0) {
            revealedCount = 3;
            currentStreet = HandStage.FLOP;
            _handStage.setValue(HandStage.FLOP);
            _status.setValue("Flop revealed. Your move: check or raise.");
        } else if (revealedCount == 3) {
            revealedCount = 4;
            currentStreet = HandStage.TURN;
            _handStage.setValue(HandStage.TURN);
            _status.setValue("Turn revealed. Your move: check or raise.");
        } else if (revealedCount == 4) {
            revealedCount = 5;
            currentStreet = HandStage.RIVER;
            _handStage.setValue(HandStage.RIVER);
            _status.setValue("River revealed. Your move: check or raise.");
        } else {
            return;
        }

        updateUiCards(); // Update board URLs
        _boardCardsText.setValue(boardToStringRevealed(revealedCount)); // Legacy text update

        _revealEnabled.setValue(false);
        _revealButtonText.setValue(nextRevealButtonText());
        _bettingEnabled.setValue(true);
        updateRaiseSlider();
        updateRaiseEnabled();
    }

    private void startOrRefreshFreeCoinsTimer() {
        if (!isSignedIn()) {
            if (freeTimer != null)
                freeTimer.cancel();
            _freeAvailable.setValue(false);
            _freeTimerText.setValue("Sign in required for free coins");
            return;
        }
        if (freeTimer != null)
            freeTimer.cancel();

        repository.ensureInitialized();
        boolean available = repository.canClaimFreeCoins();
        _freeAvailable.setValue(available);

        if (available) {
            _freeTimerText.setValue("Free +10 coins ready!");
            return;
        }

        long nextAt = repository.getNextFreeCoinsAtMs();
        long now = System.currentTimeMillis();
        long millisLeft = Math.max(0, nextAt - now);

        freeTimer = new CountDownTimer(millisLeft, 1000) {
            @Override
            public void onTick(long ms) {
                _freeTimerText.setValue("Next free coins in: " + formatMillis(ms));
            }

            @Override
            public void onFinish() {
                refreshCoins();
                _freeTimerText.setValue("Free +10 coins ready!");
            }
        }.start();
    }

    private String formatMillis(long ms) {
        long totalSeconds = ms / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

# UnlimitedGames
Android flash-game exploration.

## Overview
UnlimitedGames is a modular Android application comprising multiple mini-games. It uses a centralized dashboard to navigate to different game fragments. The app manages user profiles, friendships, and global/friend leaderboards using Firebase Firestore.

## Implemented Games
- **2048**: Classic tile-merging puzzle.
- **Sudoku**: Number placement puzzle.
- **Soccer Separation**: A unique physics/logic game.
- **Whack-a-Mole**: Fast-paced arcade game.
- **Maze**: Pathfinding puzzle.
- **Poker**: Classic card game.

## Developer Guide

### Architecture
- **Navigation**: The app uses Android Jetpack Navigation. Each game is hosted in its own `Fragment`.
- **Data Source**: `GameDataSource.java` acts as the registry for all available games.
- **Data Cleanup**: The `IGame` interface ensures uniform cleanup of user data (e.g., cached saves) on logout.
- **Backend**: Firebase Firestore handles Users, Friends (relationships), and Scores.

### How to Implement a New Game

To add a new game to the application, follow these steps:

#### 1. Create the Game Logic & UI
Create a new package under `com.appsters.unlimitedgames.games.[yourgame]`. Implement your game logic within a `Fragment`.

#### 2. Define the Game Type
Add a new entry to the `GameType` enum (`com.appsters.unlimitedgames.app.util.GameType`).
```java
public enum GameType {
    // ... other games
    YOUR_NEW_GAME
}
```

#### 3. Register the Game
Update `com.appsters.unlimitedgames.app.data.GameDataSource` to include your game in the list.
```java
games.add(new Game(
    "id",
    GameType.YOUR_NEW_GAME,
    "Game Title",
    "Description",
    R.id.action_to_yourGameFragment, // Navigation ID from nav_graph.xml
    R.drawable.game_icon
));
```

#### 4. Implement Data Cleanup
Implement the `IGame` interface (`com.appsters.unlimitedgames.games.interfaces.IGame`) in your game's main class or manager. This is crucial for clearing local state when a user logs out.
```java
public class MyGame implements IGame {
    @Override
    public void clearUserData() {
        // Clear shared preferences, local databases, or cached files
    }
}
```
Then, register this implementation in `GameDataSource.clearAllGameData()`.

### Handling High Scores

The app uses a centralized `LeaderboardRepository` to manage high scores.

#### Submitting a Score
Inject or instantiate `LeaderboardRepository` and call `submitScore`. behavior:
- It automatically checks if the new score is higher than the user's existing high score for that game.
- If it is a new high score, it updates Firestore.
- It respects the user's privacy settings (Public/Friends Only).

```java
LeaderboardRepository repo = new LeaderboardRepository(context);
Score newScore = new Score(
    null, // scoreId (auto-generated)
    currentUserId,
    username,
    GameType.YOUR_NEW_GAME,
    scoreValue,
    privacy
);

repo.submitScore(newScore, (success, result, error) -> {
    if (success) {
        // Score submitted
    }
});
```

## Setup Instructions

### Prerequisites
- Android Studio (latest version)
- JDK 11 or higher
- Git

### Initial Setup
1. **Clone the repository**
2. **Get Firebase Configuration**: Place `google-services.json` in the `app/` folder.
3. **Run the app**: Build and run via Android Studio.

## Build Status
[![Actions Status][gh-actions-badge]][gh-actions]

[gh-actions]: https://github.com/Dood345/UnlimitedGames/actions
[gh-actions-badge]: https://github.com/Dood345/UnlimitedGames/workflows/Android%20CI/badge.svg

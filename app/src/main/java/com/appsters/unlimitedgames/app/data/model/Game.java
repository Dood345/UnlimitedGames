package com.appsters.unlimitedgames.app.data.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

import com.appsters.unlimitedgames.app.util.GameType;

/**
 * Represents a game in the application.
 */
public class Game {
    /** The unique ID of the game. */
    private String gameId;
    /** The type of the game. */
    private GameType gameType;
    /** The title of the game. */
    private String title;
    /** The description of the game. */
    private String description;
    /** The navigation action ID for the game. */
    private int actionId;
    /** The drawable resource ID for the game's image. */
    private int imageResId;

    /**
     * Default constructor required for frameworks like Firebase.
     */
    public Game() {}

    /**
     * Constructs a new Game with the specified details.
     *
     * @param gameId      The unique ID of the game.
     * @param gameType    The type of the game.
     * @param title       The title of the game.
     * @param description The description of the game.
     * @param actionId    The navigation action ID for the game.
     * @param imageResId  The drawable resource ID for the game's image.
     */
    public Game(String gameId, GameType gameType, String title, String description, @IdRes int actionId, @DrawableRes int imageResId) {
        this.gameId = gameId;
        this.gameType = gameType;
        this.title = title;
        this.description = description;
        this.actionId = actionId;
        this.imageResId = imageResId;
    }

    /**
     * Gets the game ID.
     *
     * @return The game ID.
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Sets the game ID.
     *
     * @param gameId The new game ID.
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets the game type.
     *
     * @return The game type.
     */
    public GameType getGameType() {
        return gameType;
    }

    /**
     * Sets the game type.
     *
     * @param gameType The new game type.
     */
    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    /**
     * Gets the title.
     *
     * @return The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title The new title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the description.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description The new description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the navigation action ID.
     *
     * @return The navigation action ID.
     */
    @IdRes
    public int getActionId() {
        return actionId;
    }

    /**
     * Sets the navigation action ID.
     *
     * @param actionId The new navigation action ID.
     */
    public void setActionId(@IdRes int actionId) {
        this.actionId = actionId;
    }

    /**
     * Gets the image resource ID.
     *
     * @return The image resource ID.
     */
    @DrawableRes
    public int getImageResId() {
        return imageResId;
    }

    /**
     * Sets the image resource ID.
     *
     * @param imageResId The new image resource ID.
     */
    public void setImageResId(@DrawableRes int imageResId) {
        this.imageResId = imageResId;
    }
}

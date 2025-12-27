package com.appsters.simpleGames.games.poker.model;

public class Card {
    public final int rank; // 2..14 (14 = Ace)
    public final Suit suit;

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES
    }

    public Card(int rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public String rankToString() {
        switch (rank) {
            case 14:
                return "A";
            case 13:
                return "K";
            case 12:
                return "Q";
            case 11:
                return "J";
            case 10:
                return "0"; // API uses '0' for 10
            default:
                return String.valueOf(rank);
        }
    }

    public String suitToString() {
        switch (suit) {
            case CLUBS:
                return "C";
            case DIAMONDS:
                return "D";
            case HEARTS:
                return "H";
            case SPADES:
                return "S";
            default:
                return "";
        }
    }

    public String getCode() {
        return rankToString() + suitToString();
    }

    public String getImageUrl() {
        return "https://deckofcardsapi.com/static/img/" + getCode() + ".png";
    }

    @Override
    public String toString() {
        // Keep old toString for debugging if needed, or update.
        // Let's keep a readable format for logs.
        String r = rankToString();
        if (r.equals("0"))
            r = "10";
        String s = "?";
        switch (suit) {
            case CLUBS:
                s = "♣";
                break;
            case DIAMONDS:
                s = "♦";
                break;
            case HEARTS:
                s = "♥";
                break;
            case SPADES:
                s = "♠";
                break;
        }
        return r + s;
    }
}

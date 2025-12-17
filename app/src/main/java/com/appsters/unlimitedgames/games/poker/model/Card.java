package com.appsters.unlimitedgames.games.poker.model;

public class Card {
    public final int rank; // 2..14 (14 = Ace)
    public final Suit suit;

    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    public Card(int rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public String rankToString() {
        switch (rank) {
            case 14: return "A";
            case 13: return "K";
            case 12: return "Q";
            case 11: return "J";
            case 10: return "10";
            default: return String.valueOf(rank);
        }
    }

    public String suitToString() {
        switch (suit) {
            case CLUBS: return "♣";
            case DIAMONDS: return "♦";
            case HEARTS: return "♥";
            case SPADES: return "♠";
            default: return "?";
        }
    }

    @Override
    public String toString() {
        return rankToString() + suitToString();
    }
}

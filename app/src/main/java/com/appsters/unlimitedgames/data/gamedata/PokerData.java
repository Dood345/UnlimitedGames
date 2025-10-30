package com.appsters.unlimitedgames.data.gamedata;

import java.util.ArrayList;
import java.util.List;

public class PokerData {
    private List<Card> hand;
    private int chips;
    private int currentBet;

    public PokerData() {
        this.hand = new ArrayList<>();
        this.chips = 1000; // Starting chips
        this.currentBet = 0;
    }

    // TODO: Add poker game logic methods
    // public void dealCards() {}
    // public void fold() {}
    // public void call() {}
    // public void raise(int amount) {}

    // Getters and setters
    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public static class Card {
        private final Suit suit;
        private final Rank rank;

        public Card(Suit suit, Rank rank) {
            this.suit = suit;
            this.rank = rank;
        }

        public Suit getSuit() {
            return suit;
        }

        public Rank getRank() {
            return rank;
        }

        @Override
        public String toString() {
            return rank + " of " + suit;
        }
    }

    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    public enum Rank {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
    }
}

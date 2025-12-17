package com.appsters.unlimitedgames.games.poker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {
    private final List<Card> cards = new ArrayList<>();
    private int index = 0;

    public Deck() {
        for (Card.Suit s : Card.Suit.values()) {
            for (int r = 2; r <= 14; r++) {
                cards.add(new Card(r, s));
            }
        }
    }

    public void shuffle(Random random) {
        Collections.shuffle(cards, random);
        index = 0;
    }

    public Card draw() {
        if (index >= cards.size()) throw new IllegalStateException("Deck is empty");
        return cards.get(index++);
    }
}

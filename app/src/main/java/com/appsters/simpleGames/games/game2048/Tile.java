package com.appsters.simpleGames.games.game2048;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Tile {
    private final String id;
    private final int value;
    private final List<String> mergedFromIds;

    public Tile(int value) {
        this.id = UUID.randomUUID().toString();
        this.value = value;
        this.mergedFromIds = new ArrayList<>();
    }

    public Tile(int value, List<String> mergedFromIds) {
        this.id = UUID.randomUUID().toString();
        this.value = value;
        this.mergedFromIds = mergedFromIds != null ? new ArrayList<>(mergedFromIds) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public List<String> getMergedFromIds() {
        return mergedFromIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tile tile = (Tile) o;
        return value == tile.value && Objects.equals(id, tile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }
}

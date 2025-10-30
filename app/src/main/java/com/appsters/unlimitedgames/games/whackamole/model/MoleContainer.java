package com.appsters.unlimitedgames.games.whackamole.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable container for a list of moles.
 * Tracks which mole is currently visible.
 */
public final class MoleContainer {

    private final List<Mole> moles;
    private final int visibleMoleId;

    /**
     * Constructor
     *
     * @param totalMoles    total number of moles
     * @param visibleMoleId the ID of the mole that should be visible initially
     * @throws IllegalArgumentException if visibleMoleId is out of bounds
     */
    public MoleContainer(int totalMoles, int visibleMoleId) {
        if (totalMoles <= 0) {
            throw new IllegalArgumentException("totalMoles must be greater than 0");
        }
        if (visibleMoleId < 0 || visibleMoleId >= totalMoles) {
            throw new IllegalArgumentException(
                    "visibleMoleId must be between 0 and totalMoles - 1"
            );
        }

        List<Mole> tempList = new ArrayList<>();
        for (int i = 0; i < totalMoles; i++) {
            tempList.add(new Mole(i, i == visibleMoleId));
        }

        this.moles = Collections.unmodifiableList(tempList);
        this.visibleMoleId = visibleMoleId;
    }

    /**
     * Get the ID of the currently visible mole
     *
     * @return visible mole ID
     */
    public int getVisibleId() {
        return visibleMoleId;
    }

    /**
     * Get the list of moles
     *
     * @return unmodifiable list of moles
     */
    public List<Mole> getMoles() {
        return moles;
    }
}

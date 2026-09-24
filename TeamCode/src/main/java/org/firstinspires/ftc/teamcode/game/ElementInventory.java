package org.firstinspires.ftc.teamcode.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Ordered, capacity-limited model of the elements physically controlled by the robot. */
public final class ElementInventory {
    public static final int CAPACITY = 4;

    public enum AllianceColor { RED, BLUE }

    public enum ScoringElement {
        POLLEN, RED_NECTAR, BLUE_NECTAR;

        boolean belongsTo(AllianceColor alliance) {
            return this == POLLEN
                    || alliance == AllianceColor.RED && this == RED_NECTAR
                    || alliance == AllianceColor.BLUE && this == BLUE_NECTAR;
        }
    }

    private AllianceColor alliance;
    private final Deque<ScoringElement> elements = new ArrayDeque<>();

    public ElementInventory(AllianceColor alliance) {
        setAlliance(alliance);
    }

    public synchronized void setAlliance(AllianceColor alliance) {
        if (alliance == null) throw new IllegalArgumentException("alliance is required");
        this.alliance = alliance;
        elements.clear();
    }

    public synchronized boolean tryAdd(ScoringElement element) {
        if (element == null || !element.belongsTo(alliance) || elements.size() >= CAPACITY) {
            return false;
        }
        elements.addLast(element);
        return true;
    }

    /** Removes the oldest indexed element after a confirmed feed or deposit. */
    public synchronized ScoringElement releaseNext() {
        return elements.pollFirst();
    }

    /** Removes the newest element when the intake reverses it back to the field. */
    public synchronized ScoringElement rejectNewest() {
        return elements.pollLast();
    }

    public synchronized int size() {
        return elements.size();
    }

    public synchronized ScoringElement peekNext() {
        return elements.peekFirst();
    }

    public synchronized List<ScoringElement> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(elements));
    }

    public synchronized void clear() {
        elements.clear();
    }
}

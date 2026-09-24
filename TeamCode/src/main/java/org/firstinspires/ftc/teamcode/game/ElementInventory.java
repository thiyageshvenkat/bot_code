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

    private final AllianceColor alliance;
    private final Deque<ScoringElement> elements = new ArrayDeque<>();

    public ElementInventory(AllianceColor alliance) {
        if (alliance == null) throw new IllegalArgumentException("alliance is required");
        this.alliance = alliance;
    }

    public synchronized boolean tryAdd(ScoringElement element) {
        if (!canCollect(alliance, element, elements.size())) return false;
        elements.addLast(element);
        return true;
    }

    public static boolean canControlAnother(int controlledCount) {
        return controlledCount >= 0 && controlledCount < CAPACITY;
    }

    public static boolean canCollect(AllianceColor alliance, ScoringElement element,
                                     int controlledCount) {
        return element != null && element.belongsTo(alliance)
                && canControlAnother(controlledCount);
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

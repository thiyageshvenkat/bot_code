package org.firstinspires.ftc.teamcode.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Ordered, capacity-limited model of the elements physically controlled by the robot. */
public final class ElementInventory {
    private final AllianceColor alliance;
    private final Deque<ScoringElement> elements = new ArrayDeque<>();

    public ElementInventory(AllianceColor alliance) {
        if (alliance == null) throw new IllegalArgumentException("alliance is required");
        this.alliance = alliance;
    }

    public synchronized boolean tryAdd(ScoringElement element) {
        if (!BiobuzzRules.canCollect(alliance, element, elements.size())) return false;
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

    public synchronized int remainingCapacity() {
        return Math.max(0, org.firstinspires.ftc.teamcode.constants.RobotConfig.Magazine.CAPACITY
                - elements.size());
    }

    public synchronized boolean isFull() {
        return !BiobuzzRules.canControlAnother(elements.size());
    }

    public synchronized ScoringElement peekNext() {
        return elements.peekFirst();
    }

    public synchronized int count(ScoringElement type) {
        int count = 0;
        for (ScoringElement element : elements) if (element == type) count++;
        return count;
    }

    public synchronized List<ScoringElement> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(elements));
    }

    public synchronized void clear() {
        elements.clear();
    }
}

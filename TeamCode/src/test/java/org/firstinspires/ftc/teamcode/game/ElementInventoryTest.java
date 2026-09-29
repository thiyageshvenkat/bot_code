package org.firstinspires.ftc.teamcode.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ElementInventoryTest {
    @Test public void preservesFeedOrderAndCapsAtFour() {
        ElementInventory inventory = new ElementInventory(AllianceColor.BLUE);
        assertFalse(inventory.isFull());
        assertTrue(inventory.tryAdd(ScoringElement.POLLEN));
        assertTrue(inventory.tryAdd(ScoringElement.BLUE_NECTAR));
        assertTrue(inventory.tryAdd(ScoringElement.POLLEN));
        assertTrue(inventory.tryAdd(ScoringElement.POLLEN));
        assertTrue(inventory.isFull());
        assertFalse(inventory.tryAdd(ScoringElement.POLLEN));
        assertEquals(4, inventory.size());
        assertEquals(ScoringElement.POLLEN, inventory.releaseNext());
        assertFalse(inventory.isFull());
        assertEquals(ScoringElement.BLUE_NECTAR, inventory.releaseNext());
    }

    @Test public void rejectsOpposingNectarWithoutChangingCount() {
        ElementInventory inventory = new ElementInventory(AllianceColor.RED);
        assertFalse(inventory.tryAdd(ScoringElement.BLUE_NECTAR));
        assertEquals(0, inventory.size());
        assertNull(inventory.peekNext());
    }

    @Test public void reverseRejectsNewestElement() {
        ElementInventory inventory = new ElementInventory(AllianceColor.RED);
        inventory.tryAdd(ScoringElement.POLLEN);
        inventory.tryAdd(ScoringElement.RED_NECTAR);
        assertEquals(ScoringElement.RED_NECTAR, inventory.rejectNewest());
        assertEquals(ScoringElement.POLLEN, inventory.peekNext());
    }

    @Test public void sensorlessDriverCanFeedAgainAfterFourPulsesButAutoCannot() {
        ElementInventory inventory = new ElementInventory(AllianceColor.RED);
        for (int i = 0; i < ElementInventory.CAPACITY; i++) {
            inventory.tryAdd(ScoringElement.POLLEN);
        }
        for (int i = 0; i < ElementInventory.CAPACITY; i++) {
            assertTrue(inventory.permitsFeed(true));
            inventory.releaseNext();
        }
        assertFalse(inventory.permitsFeed(true));
        assertTrue(inventory.permitsFeed(false));
        inventory.releaseNext();
        assertTrue(inventory.permitsFeed(false));
        assertEquals(0, inventory.size());
    }

    @Test public void constructorRejectsMissingAlliance() {
        assertThrows(IllegalArgumentException.class, () -> new ElementInventory(null));
    }

    @Test public void nullAndBothOpponentNectarColorsAreRejectedWithoutMutation() {
        for (AllianceColor alliance : AllianceColor.values()) {
            ElementInventory inventory = new ElementInventory(alliance);
            assertFalse(inventory.tryAdd(null));
            ScoringElement opponent = alliance == AllianceColor.RED
                    ? ScoringElement.BLUE_NECTAR : ScoringElement.RED_NECTAR;
            assertFalse(inventory.tryAdd(opponent));
            assertTrue(inventory.tryAdd(ScoringElement.POLLEN));
            assertTrue(inventory.tryAdd(alliance == AllianceColor.RED
                    ? ScoringElement.RED_NECTAR : ScoringElement.BLUE_NECTAR));
            assertEquals(2, inventory.size());
        }
    }

    @Test public void emptyRemovalAndRepeatedClearAreSafe() {
        ElementInventory inventory = new ElementInventory(AllianceColor.RED);
        assertNull(inventory.releaseNext());
        assertNull(inventory.rejectNewest());
        inventory.clear();
        inventory.clear();
        assertEquals(0, inventory.size());
        assertFalse(inventory.isFull());
        assertNull(inventory.peekNext());
    }

    @Test public void snapshotIsOrderedDetachedAndUnmodifiable() {
        ElementInventory inventory = new ElementInventory(AllianceColor.BLUE);
        inventory.tryAdd(ScoringElement.POLLEN);
        inventory.tryAdd(ScoringElement.BLUE_NECTAR);
        List<ScoringElement> snapshot = inventory.snapshot();
        assertEquals(Arrays.asList(ScoringElement.POLLEN, ScoringElement.BLUE_NECTAR), snapshot);
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(ScoringElement.POLLEN));

        inventory.releaseNext();
        assertEquals(Arrays.asList(ScoringElement.POLLEN, ScoringElement.BLUE_NECTAR), snapshot);
        assertEquals(Arrays.asList(ScoringElement.BLUE_NECTAR), inventory.snapshot());
    }

    @Test public void randomizedOperationsMatchAReferenceQueue() {
        for (AllianceColor alliance : AllianceColor.values()) {
            ElementInventory actual = new ElementInventory(alliance);
            Deque<ScoringElement> expected = new ArrayDeque<>();
            Random random = new Random(23229 + alliance.ordinal());
            for (int operation = 0; operation < 25_000; operation++) {
                switch (random.nextInt(6)) {
                    case 0:
                    case 1:
                    case 2:
                        ScoringElement candidate = randomElementOrNull(random);
                        boolean shouldAdd = candidate != null && candidate.belongsTo(alliance)
                                && expected.size() < ElementInventory.CAPACITY;
                        assertEquals(shouldAdd, actual.tryAdd(candidate));
                        if (shouldAdd) expected.addLast(candidate);
                        break;
                    case 3:
                        assertEquals(expected.pollFirst(), actual.releaseNext());
                        break;
                    case 4:
                        assertEquals(expected.pollLast(), actual.rejectNewest());
                        break;
                    default:
                        expected.clear();
                        actual.clear();
                        break;
                }
                assertEquals(expected.size(), actual.size());
                assertEquals(expected.size() == ElementInventory.CAPACITY, actual.isFull());
                assertEquals(expected.peekFirst(), actual.peekNext());
                assertEquals(new ArrayList<>(expected), actual.snapshot());
                assertEquals(!expected.isEmpty(), actual.permitsFeed(true));
                assertTrue(actual.permitsFeed(false));
            }
        }
    }

    @Test public void simultaneousAddsNeverExceedCapacity() throws Exception {
        final int workerCount = 32;
        ElementInventory inventory = new ElementInventory(AllianceColor.RED);
        ExecutorService workers = Executors.newFixedThreadPool(workerCount);
        CountDownLatch ready = new CountDownLatch(workerCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 0; i < workerCount; i++) {
                results.add(workers.submit(() -> {
                    ready.countDown();
                    start.await();
                    return inventory.tryAdd(ScoringElement.POLLEN);
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            int successfulAdds = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) successfulAdds++;
            }
            assertEquals(ElementInventory.CAPACITY, successfulAdds);
            assertEquals(ElementInventory.CAPACITY, inventory.size());
            assertTrue(inventory.isFull());
        } finally {
            start.countDown();
            workers.shutdownNow();
        }
    }

    private static ScoringElement randomElementOrNull(Random random) {
        int selection = random.nextInt(ScoringElement.values().length + 1);
        return selection == ScoringElement.values().length
                ? null : ScoringElement.values()[selection];
    }
}

package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LimelightReaderTest {
    private double oldMaximumFrameAge;

    @Before
    public void useKnownMaximumFrameAge() {
        oldMaximumFrameAge = RobotConfig.Vision.MAX_FRAME_AGE_MS;
        RobotConfig.Vision.MAX_FRAME_AGE_MS = 150;
    }

    @After
    public void restoreMaximumFrameAge() {
        RobotConfig.Vision.MAX_FRAME_AGE_MS = oldMaximumFrameAge;
    }

    @Test
    public void rejectsImagesFromBeforePipelineSelection() {
        LimelightReader.LatestCameraFrameCheck latestFrameCheck =
                new LimelightReader.LatestCameraFrameCheck();
        latestFrameCheck.expectPipeline(1, 100);
        assertFalse(latestFrameCheck.checkIfLatestCameraFrame(0, 101, 0, 0));
        assertFalse(latestFrameCheck.checkIfLatestCameraFrame(1, 100, 0, 0));
        assertTrue(latestFrameCheck.checkIfLatestCameraFrame(1, 101, 0, 0));
        latestFrameCheck.expectPipeline(0, 101);
        assertFalse(latestFrameCheck.checkIfLatestCameraFrame(1, 102, 0, 0));
        assertTrue(latestFrameCheck.checkIfLatestCameraFrame(0, 102, 0, 0));
    }

    @Test
    public void rejectsOldAndRepeatedCameraImages() {
        LimelightReader.LatestCameraFrameCheck latestFrameCheck =
                new LimelightReader.LatestCameraFrameCheck();
        latestFrameCheck.expectPipeline(1, Double.NaN);
        assertFalse(latestFrameCheck.checkIfLatestCameraFrame(1, 100, 200, 0));
        assertFalse(latestFrameCheck.checkIfLatestCameraFrame(1, 100, Double.NaN, 0));
        assertFalse(latestFrameCheck.checkIfLatestCameraFrame(1, 0, 0, 0));
        assertFalse(latestFrameCheck.checkIfLatestCameraFrame(1, Double.NaN, 0, 0));
        assertTrue(latestFrameCheck.checkIfLatestCameraFrame(1, 100, 0, 0));
        assertFalse(latestFrameCheck.checkIfLatestCameraFrame(1, 100, 0, 200_000_000));
        assertTrue(latestFrameCheck.checkIfLatestCameraFrame(1, 101, 0, 210_000_000));
    }

    @Test
    public void acceptsExactAgeBoundaryAndRejectsAnyExcess() {
        LimelightReader.LatestCameraFrameCheck check = newCheck(1);
        assertTrue(check.checkIfLatestCameraFrame(1, 1, 150, 1_000_000_000));
        assertFalse(check.checkIfLatestCameraFrame(1, 2, 150.000001, 1_000_000_000));
    }

    @Test
    public void repeatedFrameIncludesTimeSinceItWasFirstObserved() {
        LimelightReader.LatestCameraFrameCheck check = newCheck(1);
        long firstSeen = 5_000_000_000L;
        assertTrue(check.checkIfLatestCameraFrame(1, 10, 25, firstSeen));
        assertTrue(check.checkIfLatestCameraFrame(1, 10, 25,
                firstSeen + 125_000_000L));
        assertFalse(check.checkIfLatestCameraFrame(1, 10, 25,
                firstSeen + 125_000_001L));
    }

    @Test
    public void newFrameRecoversImmediatelyAfterFrozenFrameWasRejected() {
        LimelightReader.LatestCameraFrameCheck check = newCheck(1);
        assertTrue(check.checkIfLatestCameraFrame(1, 10, 0, 0));
        assertFalse(check.checkIfLatestCameraFrame(1, 10, 0, 200_000_000));
        assertTrue(check.checkIfLatestCameraFrame(1, 11, 0, 200_000_001));
    }

    @Test
    public void rejectsEveryInvalidTimestampAndReportedAge() {
        for (double timestamp : new double[] {
                Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -1, 0
        }) {
            assertFalse(newCheck(1).checkIfLatestCameraFrame(1, timestamp, 0, 0));
        }
        for (double age : new double[] {
                Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -0.000001
        }) {
            assertFalse(newCheck(1).checkIfLatestCameraFrame(1, 1, age, 0));
        }
    }

    @Test
    public void changingExpectedPipelineResetsTimestampHistory() {
        LimelightReader.LatestCameraFrameCheck check = newCheck(1);
        assertTrue(check.checkIfLatestCameraFrame(1, 10, 0, 0));
        check.expectPipeline(2, 10);
        assertFalse(check.checkIfLatestCameraFrame(1, 11, 0, 0));
        assertFalse(check.checkIfLatestCameraFrame(2, 10, 0, 0));
        assertTrue(check.checkIfLatestCameraFrame(2, 11, 0, 0));
    }

    @Test
    public void ageLimitChangesTakeEffectWithoutRecreatingChecker() {
        LimelightReader.LatestCameraFrameCheck check = newCheck(1);
        assertTrue(check.checkIfLatestCameraFrame(1, 1, 100, 0));
        RobotConfig.Vision.MAX_FRAME_AGE_MS = 50;
        assertFalse(check.checkIfLatestCameraFrame(1, 2, 100, 1));
        assertTrue(check.checkIfLatestCameraFrame(1, 3, 50, 2));
    }

    private static LimelightReader.LatestCameraFrameCheck newCheck(int pipeline) {
        LimelightReader.LatestCameraFrameCheck check =
                new LimelightReader.LatestCameraFrameCheck();
        check.expectPipeline(pipeline, Double.NaN);
        return check;
    }
}

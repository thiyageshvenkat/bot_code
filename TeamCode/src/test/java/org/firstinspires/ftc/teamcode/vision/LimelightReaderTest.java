package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LimelightReaderTest {
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
}

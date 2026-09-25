package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Starts one Limelight on its assigned pipeline and returns only current results from that camera.
 * Pipeline requests run outside the robot update loop because an unplugged camera may take time to
 * answer. Failed requests are retried without pausing drivetrain or shooter updates.
 */
final class LimelightReader implements AutoCloseable {
    // Read quickly enough for aiming without asking the Control Hub for more than 50 updates/second.
    private static final int CAMERA_POLL_RATE_HZ = 50;
    // Wait half a second between failed pipeline requests instead of sending one every robot loop.
    private static final long PIPELINE_RETRY_DELAY_NANOS = 500_000_000L;

    // Physical Limelight owned by this reader.
    private final Limelight3A camera;
    // Pipeline this camera must report before any result is returned.
    private final int pipeline;
    // Remembers camera timestamps so a frozen image eventually becomes unusable.
    private final LatestCameraFrameCheck latestFrameCheck = new LatestCameraFrameCheck();
    // A pipeline request can time out, so it runs separately from drivetrain and shooter updates.
    private final ExecutorService pipelineSelectionThread = Executors.newSingleThreadExecutor();
    // False until Limelight confirms that it accepted the requested pipeline number.
    private boolean pipelineSelectionConfirmed;
    // Earliest time another failed pipeline request may be sent.
    private long nextPipelineRequestTimeNanos;
    // Current pipeline request, or null when no request is waiting or running.
    private Future<Boolean> pipelineSelectionRequest;

    /** Configures polling, starts the camera, and requests its one assigned pipeline. */
    LimelightReader(Limelight3A camera, int pipeline) {
        this.camera = camera;
        this.pipeline = pipeline;
        camera.setPollRateHz(CAMERA_POLL_RATE_HZ);
        LLResult resultBeforePipelineSelection = camera.getLatestResult();
        latestFrameCheck.expectPipeline(pipeline, resultBeforePipelineSelection == null
                ? Double.NaN : resultBeforePipelineSelection.getTimestamp());
        camera.start();
        requestPipelineIfNeeded();
    }

    /** Connection status only; true does not mean that a usable target is visible. */
    boolean isConnected() {
        return camera.isConnected();
    }

    /**
     * Returns the newest usable camera result. A missing connection, unfinished pipeline selection,
     * wrong pipeline, invalid result, old image, or repeatedly frozen image returns null.
     */
    LLResult getLatestUsableResult() {
        checkPipelineSelectionResult();
        requestPipelineIfNeeded();
        LLResult result = camera.getLatestResult();
        if (!pipelineSelectionConfirmed || !camera.isConnected() || result == null) {
            return null;
        }
        if (result.getPipelineIndex() != pipeline) {
            pipelineSelectionConfirmed = false;
            return null;
        }

        double imageAgeMs = result.getStaleness() + result.getCaptureLatency()
                + result.getTargetingLatency();
        if (!latestFrameCheck.checkIfLatestCameraFrame(
                pipeline, result.getTimestamp(), imageAgeMs, System.nanoTime())
                || !result.isValid()) {
            return null;
        }
        return result;
    }

    private void checkPipelineSelectionResult() {
        // Reading Future.get() is safe only after isDone(); otherwise the robot loop could pause.
        if (pipelineSelectionRequest == null || !pipelineSelectionRequest.isDone()) {
            return;
        }
        try {
            pipelineSelectionConfirmed = pipelineSelectionRequest.get();
        } catch (ExecutionException e) {
            // Limelight rejected the request or communication failed. A later loop will retry.
            pipelineSelectionConfirmed = false;
        } catch (InterruptedException e) {
            // Preserve the stop request and leave this camera unusable.
            Thread.currentThread().interrupt();
            pipelineSelectionConfirmed = false;
        }
        pipelineSelectionRequest = null;
    }

    private void requestPipelineIfNeeded() {
        long now = System.nanoTime();
        if (!pipelineSelectionConfirmed && pipelineSelectionRequest == null
                && now >= nextPipelineRequestTimeNanos) {
            // pipelineSwitch communicates with the camera and may take time when it is unplugged.
            pipelineSelectionRequest = pipelineSelectionThread.submit(
                    () -> camera.pipelineSwitch(pipeline));
            nextPipelineRequestTimeNanos = now + PIPELINE_RETRY_DELAY_NANOS;
        }
    }

    @Override
    public void close() {
        // Stop both the pending request thread and Limelight polling when the OpMode ends.
        pipelineSelectionThread.shutdownNow();
        camera.stop();
    }

    /** Checks that a result is from the requested pipeline and the camera is still sending images. */
    static final class LatestCameraFrameCheck {
        // Only results carrying this pipeline number may be used.
        private int expectedPipeline = -1;
        // Image that existed before the pipeline request; it belongs to the previous configuration.
        private double frameTimestampBeforePipelineSelection;
        // Timestamp of the newest image seen by robot code.
        private double lastSeenFrameTimestamp = Double.NaN;
        // Control Hub time when lastSeenFrameTimestamp first changed to its current value.
        private long timeLastNewFrameSeenNanos;

        /** Resets image tracking when this reader requests its assigned pipeline. */
        void expectPipeline(int pipeline, double previousTimestamp) {
            // Do not accept an image left over from before this pipeline was requested.
            expectedPipeline = pipeline;
            frameTimestampBeforePipelineSelection = previousTimestamp;
            lastSeenFrameTimestamp = Double.NaN;
        }

        /**
         * Returns true while this image is from the requested pipeline and remains within the
         * configured age limit. Re-reading one image briefly is normal because robot code can run
         * faster than the camera; re-reading it beyond the age limit returns false.
         */
        boolean checkIfLatestCameraFrame(
                int pipeline, double timestamp, double imageAgeMs, long nowNanos) {
            // Reject the wrong pipeline, invalid timing data, the pre-selection image, and images
            // that Limelight already reports as older than the configured limit.
            if (pipeline != expectedPipeline || !Double.isFinite(timestamp) || timestamp <= 0
                    || timestamp == frameTimestampBeforePipelineSelection
                    || !Double.isFinite(imageAgeMs) || imageAgeMs < 0
                    || imageAgeMs > RobotConfig.Vision.MAX_FRAME_AGE_MS) {
                return false;
            }
            if (timestamp != lastSeenFrameTimestamp) {
                // A changed camera timestamp proves that this is a newly received image.
                lastSeenFrameTimestamp = timestamp;
                timeLastNewFrameSeenNanos = nowNanos;
            }
            // When the same image is returned again, include the time spent reusing that image.
            return imageAgeMs + (nowNanos - timeLastNewFrameSeenNanos) / 1e6
                    <= RobotConfig.Vision.MAX_FRAME_AGE_MS;
        }
    }
}

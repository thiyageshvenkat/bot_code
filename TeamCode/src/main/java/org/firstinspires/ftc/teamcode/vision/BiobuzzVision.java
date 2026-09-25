package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Uses the robot's one Limelight for pollen detection or upward-Hive-Cell targeting. */
public final class BiobuzzVision implements AutoCloseable {
    public enum Mode {
        POLLEN,
        HIVE_APRIL_TAGS
    }

    public enum HiveCell {
        AUDIENCE_SIDE,
        NON_AUDIENCE_SIDE
    }

    public static final class PollenTarget {
        public final double bearingDegrees;
        public final double areaPercent;

        private PollenTarget(double bearingDegrees, double areaPercent) {
            this.bearingDegrees = bearingDegrees;
            this.areaPercent = areaPercent;
        }
    }

    public static final class HiveTarget {
        public final HiveCell cell;
        public final double bearingDegrees;
        public final int visibleTagCount;
        public final double frameTimestampMs;
        // Camera-relative opening position and tag rotation are retained to detect Hive motion.
        final double[] openingInches;
        final double[] rotation;

        HiveTarget(HiveCell cell, double[] openingInches, double[] rotation,
                   int visibleTagCount, double frameTimestampMs) {
            this.cell = cell;
            this.bearingDegrees = Math.toDegrees(Math.atan2(openingInches[0], openingInches[2]));
            this.visibleTagCount = visibleTagCount;
            this.frameTimestampMs = frameTimestampMs;
            this.openingInches = openingInches;
            this.rotation = rotation;
        }

        /** Checks all three translation/rotation axes, not just horizontal image alignment. */
        boolean movedFrom(HiveTarget reference) {
            double squaredDistance = 0;
            double rotationTrace = 0;
            for (int i = 0; i < 3; i++) {
                squaredDistance += Math.pow(openingInches[i] - reference.openingInches[i], 2);
            }
            for (int i = 0; i < 9; i++) {
                rotationTrace += rotation[i] * reference.rotation[i];
            }
            double rotationChange = Math.toDegrees(Math.acos(
                    Math.max(-1, Math.min(1, (rotationTrace - 1) / 2))));
            return Math.sqrt(squaredDistance) > RobotConfig.Vision.HIVE_MAX_POSITION_DRIFT_IN
                    || rotationChange > RobotConfig.Vision.HIVE_MAX_ROTATION_DRIFT_DEGREES;
        }
    }

    private final Limelight3A limelight;
    private Mode mode;
    private final FrameGate frames = new FrameGate();
    private boolean pipelineAccepted;
    private long nextSwitchAttemptNanos;
    // Camera HTTP requests can time out. Keep them off the motor-control loop so STOP and feeder
    // timing stay responsive even when the camera is unplugged. Only this worker issues switches.
    private final ExecutorService pipelineWorker = Executors.newSingleThreadExecutor();
    private Future<Boolean> switchRequest;
    private int switchingPipeline;

    public BiobuzzVision(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, RobotConfig.Vision.LIMELIGHT);
        limelight.setPollRateHz(50);
        usePollenPipeline();
        limelight.start();
    }

    /** Selects the neural detector used while looking for loose pollen. */
    public void usePollenPipeline() {
        selectMode(Mode.POLLEN, RobotConfig.Vision.POLLEN_PIPELINE);
    }

    /** Requests the 3D tag pipeline; results remain unusable until the camera confirms the switch. */
    public void useHiveAprilTagPipeline() {
        selectMode(Mode.HIVE_APRIL_TAGS, RobotConfig.Vision.HIVE_APRILTAG_PIPELINE);
    }

    public Mode getMode() {
        return mode;
    }

    public PollenTarget bestPollen() {
        if (mode != Mode.POLLEN) {
            return null;
        }

        LLResult result = validLatestResult();
        if (result == null) {
            return null;
        }

        PollenTarget best = null;
        for (LLResultTypes.DetectorResult detection : result.getDetectorResults()) {
            boolean correctClass = RobotConfig.Vision.POLLEN_CLASS.equals(
                    detection.getClassName());
            boolean confident = detection.getConfidence() >= RobotConfig.Vision.MIN_CONFIDENCE;
            if (!correctClass || !confident) {
                continue;
            }

            PollenTarget candidate = new PollenTarget(
                    detection.getTargetXDegrees(), detection.getTargetArea());
            if (best == null || candidate.areaPercent > best.areaPercent) {
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Estimates the opening of an upright own-alliance Cell. A downward Cell may also be visible;
     * its orientation, not its presence, excludes it. This does NOT establish that the Hive has
     * stopped moving: HiveAim checks successive poses before autonomous may feed.
     */
    public HiveTarget upwardHiveTarget(AllianceColor alliance) {
        if (mode != Mode.HIVE_APRIL_TAGS || alliance == null) {
            return null;
        }

        LLResult result = validLatestResult();
        if (result == null) {
            return null;
        }

        List<HiveTarget> candidates = new ArrayList<>();
        for (LLResultTypes.FiducialResult tag : result.getFiducialResults()) {
            HiveTarget candidate = targetForTag(alliance, tag.getFiducialId(),
                    tag.getTargetPoseCameraSpace(), result.getTimestamp());
            if (candidate != null) {
                candidates.add(candidate);
            }
        }
        return selectConsistentTarget(candidates);
    }

    /**
     * Converts each tag pose into the SAME opening reference before combining detections.
     * Offsets come from SDK 12's AprilTagGameDatabase.getBioBuzzCluster (inches), not tuning.
     * Limelight's documented optical axes are right/down/forward. Its Euler angles are NOT the
     * FTC VisionPortal ftcPose angles. This assumes an upright camera facing the launcher direction;
     * verify both Hive positions with no pollen before enabling live shots (see ROBOT_SETUP).
     */
    static HiveTarget targetForTag(AllianceColor alliance, int tagId, Pose3D pose, double timestamp) {
        HiveCell cell = cellForTag(alliance, tagId);
        if (cell == null || pose == null || !Double.isFinite(timestamp) || timestamp <= 0) {
            return null;
        }
        Position position = pose.getPosition().toUnit(DistanceUnit.INCH);
        YawPitchRollAngles angles = pose.getOrientation();
        double roll = angles.getRoll(AngleUnit.RADIANS);
        double pitch = angles.getPitch(AngleUnit.RADIANS);
        double yaw = angles.getYaw(AngleUnit.RADIANS);
        if (!Double.isFinite(position.x) || !Double.isFinite(position.y)
                || !Double.isFinite(position.z) || position.z <= 0
                || !Double.isFinite(roll) || !Double.isFinite(pitch) || !Double.isFinite(yaw)) {
            return null;
        }
        // Rz(yaw) * Ry(pitch) * Rx(roll): tag-local vectors expressed in camera coordinates.
        double cr = Math.cos(roll), sr = Math.sin(roll);
        double cp = Math.cos(pitch), sp = Math.sin(pitch);
        double cy = Math.cos(yaw), sy = Math.sin(yaw);
        double[] rotation = {cy * cp, cy * sp * sr - sy * cr, cy * sp * cr + sy * sr,
                sy * cp, sy * sp * sr + cy * cr, sy * sp * cr - cy * sr,
                -sp, cp * sr, cp * cr};
        // The tag's down axis must project down in the upright camera. A small margin rejects
        // near-edge-on/sideways poses instead of interpreting them as confidently upright.
        if (rotation[4] <= Math.sin(Math.toRadians(RobotConfig.Vision.HIVE_UPRIGHT_MARGIN_DEGREES))) {
            return null;
        }
        double[] tagX = {-6.50, -2.75, 2.75, 6.50};
        double[] tagOffset = {tagX[(tagId - 30) % 4], 7.1874, -5.622};
        double[] opening = {position.x, position.y, position.z};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                opening[row] -= rotation[row * 3 + column] * tagOffset[column];
            }
        }
        if (opening[2] <= 0) {
            return null;
        }
        return new HiveTarget(cell, opening, rotation, 1, timestamp);
    }

    static HiveTarget selectConsistentTarget(List<HiveTarget> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        HiveTarget reference = candidates.get(0);
        double[] opening = new double[3];
        for (HiveTarget candidate : candidates) {
            if (candidate.cell != reference.cell || candidate.movedFrom(reference)) {
                // Contradictory cell/pose estimates must not authorize a feed.
                return null;
            }
            for (int i = 0; i < 3; i++) {
                opening[i] += candidate.openingInches[i] / candidates.size();
            }
        }
        return new HiveTarget(reference.cell, opening, reference.rotation,
                candidates.size(), reference.frameTimestampMs);
    }

    /** Maps the official BIOBUZZ tag ranges to the Cell carrying each cluster. */
    static HiveCell cellForTag(AllianceColor alliance, int tagId) {
        if (alliance == AllianceColor.RED) {
            if (tagId >= 30 && tagId <= 33) {
                return HiveCell.NON_AUDIENCE_SIDE;
            }
            if (tagId >= 34 && tagId <= 37) {
                return HiveCell.AUDIENCE_SIDE;
            }
        } else if (alliance == AllianceColor.BLUE) {
            if (tagId >= 38 && tagId <= 41) {
                return HiveCell.AUDIENCE_SIDE;
            }
            if (tagId >= 42 && tagId <= 45) {
                return HiveCell.NON_AUDIENCE_SIDE;
            }
        }
        return null;
    }

    public boolean isConnected() {
        return limelight.isConnected();
    }

    @Override
    public void close() {
        pipelineWorker.shutdownNow();
        limelight.stop();
    }

    private void selectMode(Mode nextMode, int pipelineIndex) {
        if (mode != nextMode || frames.pipelineIndex != pipelineIndex) {
            LLResult previous = limelight.getLatestResult();
            double previousTimestamp = Double.NaN;
            if (previous != null) {
                previousTimestamp = previous.getTimestamp();
            }
            frames.switchTo(pipelineIndex, previousTimestamp);
            mode = nextMode;
            pipelineAccepted = false;
            nextSwitchAttemptNanos = 0;
        }
        long now = System.nanoTime();
        if (switchRequest != null && switchRequest.isDone()) {
            try {
                pipelineAccepted = switchRequest.get() && switchingPipeline == pipelineIndex;
            } catch (ExecutionException e) {
                pipelineAccepted = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                pipelineAccepted = false;
            }
            switchRequest = null;
        }
        if (!pipelineAccepted && switchRequest == null && now >= nextSwitchAttemptNanos) {
            switchingPipeline = pipelineIndex;
            switchRequest = pipelineWorker.submit(() -> limelight.pipelineSwitch(pipelineIndex));
            // Retry transient failures, without sending a new HTTP request every control loop.
            nextSwitchAttemptNanos = now + 500_000_000L;
        }
    }

    private LLResult validLatestResult() {
        // Auto requests its mode only at INIT, so reads must also service a failed switch retry.
        int pipeline = RobotConfig.Vision.POLLEN_PIPELINE;
        if (mode == Mode.HIVE_APRIL_TAGS) {
            pipeline = RobotConfig.Vision.HIVE_APRILTAG_PIPELINE;
        }
        selectMode(mode, pipeline);
        LLResult result = limelight.getLatestResult();
        if (!pipelineAccepted || !limelight.isConnected() || result == null) {
            return null;
        }
        if (result.getPipelineIndex() != pipeline && System.nanoTime() >= nextSwitchAttemptNanos) {
            pipelineAccepted = false;
        }
        double ageMs = result.getStaleness() + result.getCaptureLatency() + result.getTargetingLatency();
        if (!frames.accepts(result.getPipelineIndex(), result.getTimestamp(), ageMs, System.nanoTime())
                || !result.isValid()) {
            return null;
        }
        return result;
    }

    /** SDK polling can repeatedly retrieve one frozen camera frame; receipt age alone is insufficient. */
    static final class FrameGate {
        private int pipelineIndex = -1;
        private double switchTimestamp;
        private double lastTimestamp = Double.NaN;
        private long lastNewFrameNanos;

        void switchTo(int pipeline, double previousTimestamp) {
            pipelineIndex = pipeline;
            switchTimestamp = previousTimestamp;
            lastTimestamp = Double.NaN;
        }

        boolean accepts(int pipeline, double timestamp, double ageMs, long nowNanos) {
            if (pipeline != pipelineIndex || !Double.isFinite(timestamp) || timestamp <= 0
                    || timestamp == switchTimestamp || !Double.isFinite(ageMs) || ageMs < 0
                    || ageMs > RobotConfig.Vision.MAX_FRAME_AGE_MS) {
                return false;
            }
            if (timestamp != lastTimestamp) {
                lastTimestamp = timestamp;
                lastNewFrameNanos = nowNanos;
            }
            // Conservatively include time spent reusing a frame as well as reported latency/age.
            return ageMs + (nowNanos - lastNewFrameNanos) / 1e6 <= RobotConfig.Vision.MAX_FRAME_AGE_MS;
        }
    }
}

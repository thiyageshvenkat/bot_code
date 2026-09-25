package org.firstinspires.ftc.teamcode.vision;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/**
 * Stateful safety gate between Hive vision and an autonomous feed request.
 *
 * <p>A single centered image is not enough: it might be an old frame, a moment during a tip, or an
 * unstable 3D solve. This class therefore compares several distinct frames over time. It returns a
 * decision only; BiobuzzAutoBase retains responsibility for turning, flywheel readiness, inventory,
 * match timing, and the feeder. Keeping hardware out of this class makes its decision testable.</p>
 */
public final class HiveAim {
    // The first acceptable pose defines the reference for the whole interval. Comparing every later
    // frame to this anchor catches slow cumulative movement that adjacent-frame checks could miss.
    private BiobuzzVision.HiveTarget anchor;
    // Robot match time measures how long the observations have remained acceptable.
    private double stableSinceSeconds;
    // A long control/camera gap invalidates continuity even if the next pose happens to look similar.
    private double lastFrameSeconds;
    // Limelight timestamps distinguish a genuinely new observation from a repeatedly polled frame.
    private double lastTimestamp;
    // Time alone is insufficient; require multiple independent frames before authorizing a feed.
    private int distinctFrames;

    /**
     * Requires aligned, consistent 3D poses across several NEW frames. All drift is measured from
     * the interval's first pose so slow tipping cannot accumulate unnoticed between adjacent frames.
     * Losing a target or leaving alignment restarts the interval. A reused frame never grants a shot.
     *
     * @param target newest already-filtered own-alliance Hive opening, or null when none is usable
     * @param nowSeconds monotonic autonomous match time supplied by the OpMode
     * @return true only after the complete fresh, aligned, low-motion interval has qualified
     */
    public boolean readyToFeed(BiobuzzVision.HiveTarget target, double nowSeconds) {
        // Alignment is part of the continuous interval. Briefly leaving tolerance erases previous
        // credit instead of allowing several disconnected aligned moments to add up.
        if (target == null || !Double.isFinite(target.bearingDegrees)
                || Math.abs(target.bearingDegrees - RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES)
                > RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES) {
            reset();
            return false;
        }
        // A new cell, meaningful 3D motion, camera timestamp rollback, clock rollback, or excessive
        // frame gap begins a completely new candidate interval rather than immediately failing Auto.
        if (anchor == null || target.cell != anchor.cell || target.movedFrom(anchor)
                || target.frameTimestampMs < lastTimestamp || nowSeconds < lastFrameSeconds
                || nowSeconds - lastFrameSeconds > RobotConfig.Vision.MAX_FRAME_AGE_MS / 1000) {
            anchor = target;
            stableSinceSeconds = nowSeconds;
            lastFrameSeconds = nowSeconds;
            lastTimestamp = target.frameTimestampMs;
            distinctFrames = 1;
            return false;
        }
        // The OpMode can run faster than the camera. Re-reading one frame must not increase either
        // the independent-frame count or the last-new-frame time.
        if (target.frameTimestampMs == lastTimestamp) {
            return false;
        }
        lastTimestamp = target.frameTimestampMs;
        lastFrameSeconds = nowSeconds;
        distinctFrames++;
        return distinctFrames >= 3
                && nowSeconds - stableSinceSeconds >= RobotConfig.Vision.HIVE_AIM_HOLD_SECONDS;
    }

    /**
     * Restarts qualification after feeding, target loss, or a change of autonomous state. Old
     * timestamps may remain because a null anchor forces the next acceptable frame to replace them.
     */
    public void reset() {
        anchor = null;
        distinctFrames = 0;
    }

    /**
     * Converts Limelight's positive-right bearing convention into Pedro's positive-counterclockwise
     * turn convention and limits correction power so the robot does not violently chase a target.
     */
    public static double turnPower(double bearingDegrees) {
        double error = bearingDegrees - RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES;
        double power = -error * RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE;
        return Math.max(-RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER,
                Math.min(RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER, power));
    }
}

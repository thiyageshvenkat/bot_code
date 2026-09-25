package org.firstinspires.ftc.teamcode.vision;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/** Qualifies fresh target observations for autonomous feeding; it never commands hardware. */
public final class HiveAim {
    private BiobuzzVision.HiveTarget anchor;
    private double stableSinceSeconds;
    private double lastFrameSeconds;
    private double lastTimestamp;
    private int distinctFrames;

    /**
     * Requires aligned, consistent 3D poses across several NEW frames. All drift is measured from
     * the interval's first pose so slow tipping cannot accumulate unnoticed between adjacent frames.
     * Losing a target or leaving alignment restarts the interval. A reused frame never grants a shot.
     */
    public boolean readyToFeed(BiobuzzVision.HiveTarget target, double nowSeconds) {
        if (target == null || !Double.isFinite(target.bearingDegrees)
                || Math.abs(target.bearingDegrees - RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES)
                > RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES) {
            reset();
            return false;
        }
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
        if (target.frameTimestampMs == lastTimestamp) {
            return false;
        }
        lastTimestamp = target.frameTimestampMs;
        lastFrameSeconds = nowSeconds;
        distinctFrames++;
        return distinctFrames >= 3
                && nowSeconds - stableSinceSeconds >= RobotConfig.Vision.HIVE_AIM_HOLD_SECONDS;
    }

    /** Restarts qualification after feeding, target loss, or a change of autonomous state. */
    public void reset() {
        anchor = null;
        distinctFrames = 0;
    }

    /** Camera bearings are positive right; Pedro's positive turn is counterclockwise. */
    public static double turnPower(double bearingDegrees) {
        double error = bearingDegrees - RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES;
        double power = -error * RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE;
        return Math.max(-RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER,
                Math.min(RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER, power));
    }
}

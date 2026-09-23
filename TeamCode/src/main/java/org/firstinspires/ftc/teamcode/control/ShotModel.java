package org.firstinspires.ftc.teamcode.control;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/** Preliminary two-point shot table. Replace points with measured cell-hit data. */
public final class ShotModel {
    private ShotModel() {}

    public static ShotSolution forDistance(double distanceInches) {
        double distance = finiteOr(distanceInches, RobotConfig.Shooter.NEAR_DISTANCE_IN);
        double denominator = RobotConfig.Shooter.FAR_DISTANCE_IN
                - RobotConfig.Shooter.NEAR_DISTANCE_IN;
        double t = denominator == 0.0 ? 0.0
                : (distance - RobotConfig.Shooter.NEAR_DISTANCE_IN) / denominator;
        t = clamp(t, 0.0, 1.0);

        double hood = lerp(RobotConfig.Shooter.HOOD_NEAR,
                RobotConfig.Shooter.HOOD_FAR, t);
        double velocity = RobotConfig.Shooter.DEFAULT_VELOCITY_TPS
                * lerp(0.88, 1.12, t);
        return new ShotSolution(distance, velocity, hood);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double finiteOr(double value, double fallback) {
        return Double.isFinite(value) ? value : fallback;
    }
}

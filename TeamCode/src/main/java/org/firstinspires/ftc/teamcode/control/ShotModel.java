package org.firstinspires.ftc.teamcode.control;

import org.firstinspires.ftc.teamcode.constants.ShooterConfig;

/** Preliminary two-point shot table. Replace points with measured cell-hit data. */
public final class ShotModel {
    private ShotModel() {}

    public static ShotSolution forDistance(double distanceInches) {
        double distance = finiteOr(distanceInches, ShooterConfig.NEAR_DISTANCE_IN);
        double denominator = ShooterConfig.FAR_DISTANCE_IN
                - ShooterConfig.NEAR_DISTANCE_IN;
        double t = denominator == 0.0 ? 0.0
                : (distance - ShooterConfig.NEAR_DISTANCE_IN) / denominator;
        t = clamp(t, 0.0, 1.0);

        double hood = lerp(ShooterConfig.HOOD_NEAR,
                ShooterConfig.HOOD_FAR, t);
        double velocity = ShooterConfig.DEFAULT_VELOCITY_TPS
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

package org.firstinspires.ftc.teamcode.control;

import org.firstinspires.ftc.teamcode.constants.ShooterConfig;

/** Preliminary two-point shot table. Replace points with measured cell-hit data. */
public final class ShotModel {
    private ShotModel() {}

    public static final class Solution {
        public final double flywheelVelocity;
        public final double hoodPosition;

        private Solution(double flywheelVelocity, double hoodPosition) {
            this.flywheelVelocity = flywheelVelocity;
            this.hoodPosition = hoodPosition;
        }
    }

    public static Solution forDistance(double distanceInches) {
        double distance = Double.isFinite(distanceInches)
                ? distanceInches : ShooterConfig.NEAR_DISTANCE_IN;
        double denominator = ShooterConfig.FAR_DISTANCE_IN
                - ShooterConfig.NEAR_DISTANCE_IN;
        double t = denominator == 0.0 ? 0.0
                : (distance - ShooterConfig.NEAR_DISTANCE_IN) / denominator;
        t = Math.max(0.0, Math.min(1.0, t));
        double hood = lerp(ShooterConfig.HOOD_NEAR, ShooterConfig.HOOD_FAR, t);
        double velocity = ShooterConfig.DEFAULT_VELOCITY_TPS
                * lerp(0.88, 1.12, t);
        return new Solution(velocity, hood);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}

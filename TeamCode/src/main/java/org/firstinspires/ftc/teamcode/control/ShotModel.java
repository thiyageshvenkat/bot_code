package org.firstinspires.ftc.teamcode.control;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/** Preliminary two-point shot table. Replace points with measured cell-hit data. */
public final class ShotModel {
    public final double flywheelVelocity;
    public final double hoodPosition;

    private ShotModel(double flywheelVelocity, double hoodPosition) {
        this.flywheelVelocity = flywheelVelocity;
        this.hoodPosition = hoodPosition;
    }

    public static ShotModel forDistance(double distanceInches) {
        double distance = Double.isFinite(distanceInches)
                ? distanceInches : RobotConfig.Shooter.NEAR_DISTANCE_IN;
        double denominator = RobotConfig.Shooter.FAR_DISTANCE_IN
                - RobotConfig.Shooter.NEAR_DISTANCE_IN;
        double t = denominator == 0.0 ? 0.0
                : (distance - RobotConfig.Shooter.NEAR_DISTANCE_IN) / denominator;
        t = Math.max(0.0, Math.min(1.0, t));
        return new ShotModel(RobotConfig.Shooter.DEFAULT_VELOCITY_TPS * lerp(0.88, 1.12, t),
                lerp(RobotConfig.Shooter.HOOD_NEAR, RobotConfig.Shooter.HOOD_FAR, t));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}

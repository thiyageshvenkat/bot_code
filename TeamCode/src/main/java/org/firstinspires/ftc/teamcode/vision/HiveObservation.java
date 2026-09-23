package org.firstinspires.ftc.teamcode.vision;

public final class HiveObservation {
    public final double rangeInches;
    public final double bearingDegrees;
    public final double yawDegrees;
    public final long frameTimeNanos;

    public HiveObservation(double rangeInches, double bearingDegrees,
                           double yawDegrees, long frameTimeNanos) {
        this.rangeInches = rangeInches;
        this.bearingDegrees = bearingDegrees;
        this.yawDegrees = yawDegrees;
        this.frameTimeNanos = frameTimeNanos;
    }
}

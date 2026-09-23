package org.firstinspires.ftc.teamcode.constants;

/** Hive launcher hardware, speed qualification, and shot-table endpoints. */
public final class ShooterConfig {
    private ShooterConfig() {}

    public static final String LEFT_FLYWHEEL = "flywheel_left";
    public static final String RIGHT_FLYWHEEL = "flywheel_right";
    public static final String FEEDER = "feeder";
    public static final String HOOD = "hood";
    public static boolean RIGHT_FLYWHEEL_REVERSED = true;
    public static boolean FEEDER_REVERSED = false;
    public static double DEFAULT_VELOCITY_TPS = 1850.0; // TUNE
    public static double VELOCITY_TOLERANCE_TPS = 75.0;
    public static double READY_HOLD_SECONDS = 0.12;
    public static double FEED_POWER = 0.85;
    public static double FEED_SECONDS = 0.18;
    public static double HOOD_STOW = 0.16; // TUNE
    public static double HOOD_NEAR = 0.43; // TUNE
    public static double HOOD_FAR = 0.62; // TUNE
    public static double NEAR_DISTANCE_IN = 30.0;
    public static double FAR_DISTANCE_IN = 84.0;
}

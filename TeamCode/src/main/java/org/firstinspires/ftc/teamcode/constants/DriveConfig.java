package org.firstinspires.ftc.teamcode.constants;

/** Drive hardware names and driver/localization tuning. */
public final class DriveConfig {
    private DriveConfig() {}

    public static final String FRONT_LEFT = "front_left_drive";
    public static final String FRONT_RIGHT = "front_right_drive";
    public static final String BACK_LEFT = "back_left_drive";
    public static final String BACK_RIGHT = "back_right_drive";
    public static final String PINPOINT = "pinpoint";

    public static double STICK_DEADBAND = 0.06;
    public static double NORMAL_SCALE = 0.82;
    public static double PRECISION_SCALE = 0.35;
    public static double PINPOINT_X_OFFSET_IN = 0.0; // TUNE
    public static double PINPOINT_Y_OFFSET_IN = 0.0; // TUNE
    public static boolean FORESIGHT_TUNED = false;
}

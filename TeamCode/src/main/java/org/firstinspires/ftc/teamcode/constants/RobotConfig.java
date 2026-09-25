package org.firstinspires.ftc.teamcode.constants;

/** Hardware names and values that must be calibrated on the physical robot. */
public final class RobotConfig {
    private RobotConfig() {}

    public static final class Drive {
        private Drive() {}
        public static final String FRONT_LEFT = "front_left_drive", FRONT_RIGHT = "front_right_drive";
        public static final String BACK_LEFT = "back_left_drive", BACK_RIGHT = "back_right_drive";
        public static final String PINPOINT = "pinpoint";
        public static double STICK_DEADBAND = .06, NORMAL_SCALE = .82, PRECISION_SCALE = .35;
        public static double PINPOINT_X_OFFSET_IN = 0, PINPOINT_Y_OFFSET_IN = 0; // TUNE
        public static boolean FORESIGHT_TUNED = false;
    }

    public static final class Intake {
        private Intake() {}
        public static final String MOTOR = "intake";
        public static double COLLECT_POWER = 1, REVERSE_POWER = -.75;
    }

    public static final class Shooter {
        private Shooter() {}
        public static final String FLYWHEEL = "flywheel_left";
        public static final String FEEDER = "feeder", HOOD = "hood";
        public static boolean FEEDER_REVERSED = false;
        public static double DEFAULT_VELOCITY_TPS = 1850, VELOCITY_TOLERANCE_TPS = 75;
        public static double READY_HOLD_SECONDS = .12, FEED_POWER = .85, FEED_SECONDS = .18;
        public static double HOOD_STOW = .16, HOOD_NEAR = .43, HOOD_FAR = .62; // TUNE
        public static double NEAR_DISTANCE_IN = 30, FAR_DISTANCE_IN = 84;
    }

    public static final class Vision {
        private Vision() {}
        public static final String LIMELIGHT = "limelight", POLLEN_CLASS = "yellow_pollen";
        public static int POLLEN_PIPELINE = 0;
        public static double MIN_CONFIDENCE = .40;
    }

    public static final class Auto {
        private Auto() {}
        // Disabled: path transitions now wait for Pedro to report completion.
        // public static double PATH_TIMEOUT_SECONDS = 6;
        // Disabled: the first shot may spin up until SHOOT_CUTOFF_SECONDS.
        // public static double ALIGN_TIMEOUT_SECONDS = 2.5;
        // Stop requesting shots at 27 seconds, then begin the park path.
        public static double SHOOT_CUTOFF_SECONDS = 27;
        // Final fail-safe: stop every mechanism one second before a 30-second auto ends.
        public static double MATCH_SAFETY_CUTOFF_SECONDS = 29;
    }
}

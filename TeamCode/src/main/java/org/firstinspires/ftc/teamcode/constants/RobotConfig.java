package org.firstinspires.ftc.teamcode.constants;

/**
 * Single source of truth for hardware names and preliminary robot calibration.
 * Values marked TUNE must be measured on the competition robot.
 */
public final class RobotConfig {
    private RobotConfig() {}

    public static final class Hardware {
        private Hardware() {}

        public static final String FRONT_LEFT = "front_left_drive";
        public static final String FRONT_RIGHT = "front_right_drive";
        public static final String BACK_LEFT = "back_left_drive";
        public static final String BACK_RIGHT = "back_right_drive";
        public static final String PINPOINT = "pinpoint";

        public static final String INTAKE = "intake";
        public static final String INTAKE_BEAM = "intake_beam";
        public static final String MAGAZINE_GATE = "magazine_gate";
        public static final String MAGAZINE_EXIT_BEAM = "magazine_exit_beam";

        public static final String FLYWHEEL_LEFT = "flywheel_left";
        public static final String FLYWHEEL_RIGHT = "flywheel_right";
        public static final String FEEDER = "feeder";
        public static final String HOOD = "hood";

        public static final String FLOWER_LIFT = "flower_lift";
        public static final String FLOWER_GATE = "flower_gate";
        public static final String FLOWER_BOTTOM_LIMIT = "flower_bottom_limit";
        public static final String FLOWER_TOP_LIMIT = "flower_top_limit";

        public static final String WEBCAM = "Webcam 1";
    }

    public static final class Drive {
        private Drive() {}
        public static double STICK_DEADBAND = 0.06;
        public static double NORMAL_SCALE = 0.82;
        public static double PRECISION_SCALE = 0.35;
        public static double HEADING_HOLD_KP = 1.8; // TUNE
        public static double PINPOINT_X_OFFSET_IN = 0.0; // TUNE
        public static double PINPOINT_Y_OFFSET_IN = 0.0; // TUNE
        public static boolean FORESIGHT_TUNED = false;
    }

    public static final class Intake {
        private Intake() {}
        public static double COLLECT_POWER = 1.0;
        public static double REVERSE_POWER = -0.75;
        public static double JAM_CURRENT_AMPS = 7.0; // TUNE
        public static double JAM_TIME_SECONDS = 0.30;
        public static double CLEAR_TIME_SECONDS = 0.22;
        public static boolean BEAM_ACTIVE_LOW = true;
    }

    public static final class Magazine {
        private Magazine() {}
        public static final int CAPACITY = 4; // G407
        public static double GATE_CLOSED = 0.18; // TUNE
        public static double GATE_FEED = 0.72; // TUNE
        public static double FEED_SECONDS = 0.20;
        public static boolean EXIT_BEAM_ACTIVE_LOW = true;
    }

    public static final class Shooter {
        private Shooter() {}
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

    public static final class Flower {
        private Flower() {}
        public static double LIFT_UP_POWER = 0.85;
        public static double LIFT_DOWN_POWER = -0.55;
        public static int SCORE_TICKS = 1200; // TUNE for a 21.5-inch flower opening
        public static int POSITION_TOLERANCE_TICKS = 35;
        public static double GATE_CLOSED = 0.14; // TUNE
        public static double GATE_OPEN = 0.76; // TUNE
    }

    public static final class Vision {
        private Vision() {}
        public static double CAMERA_FORWARD_IN = 0.0; // TUNE
        public static double CAMERA_LEFT_IN = 0.0; // TUNE
        public static double CAMERA_UP_IN = 0.0; // TUNE
        public static double AIM_BEARING_TOLERANCE_DEG = 1.5;
        public static double AIM_TURN_KP = 0.025;
        public static double MAX_AIM_TURN = 0.38;
    }

    public static final class Auto {
        private Auto() {}
        public static double START_X = 12.0; // TUNE
        public static double START_Y = 12.0; // TUNE
        public static double START_HEADING_DEG = 0.0; // TUNE
        public static double SHOOT_X = 42.0; // TUNE
        public static double SHOOT_Y = 60.0; // TUNE
        public static double SHOOT_HEADING_DEG = 90.0; // TUNE
        public static double PARK_X = 12.0; // TUNE
        public static double PARK_Y = 66.0; // TUNE
        public static double PARK_HEADING_DEG = 90.0; // TUNE
        public static double PATH_TIMEOUT_SECONDS = 6.0;
        public static double ALIGN_TIMEOUT_SECONDS = 2.5;
        public static double SHOOT_CUTOFF_SECONDS = 21.5;
        public static double MATCH_SAFETY_CUTOFF_SECONDS = 29.0;
    }
}

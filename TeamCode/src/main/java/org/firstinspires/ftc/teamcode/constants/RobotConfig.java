package org.firstinspires.ftc.teamcode.constants;

/** Hardware names and values that must be calibrated on the physical robot. */
public final class RobotConfig {
    private RobotConfig() {}

    public static final class Drive {
        private Drive() {}

        // REV configuration name for the front-left drive motor.
        public static final String FRONT_LEFT = "front_left_drive";
        // REV configuration name for the front-right drive motor.
        public static final String FRONT_RIGHT = "front_right_drive";
        // REV configuration name for the back-left drive motor.
        public static final String BACK_LEFT = "back_left_drive";
        // REV configuration name for the back-right drive motor.
        public static final String BACK_RIGHT = "back_right_drive";
        // REV configuration name for the goBILDA Pinpoint odometry computer.
        public static final String PINPOINT = "pinpoint";
        // Joystick movement at or below this magnitude is treated as zero.
        public static double STICK_DEADBAND = .06;
        // Maximum drive output during normal TeleOp driving.
        public static double NORMAL_SCALE = .82;
        // Maximum drive output while the driver holds precision mode.
        public static double PRECISION_SCALE = .35;
        // Pinpoint X-pod offset in inches; replace zero with the measured value.
        public static double PINPOINT_X_OFFSET_IN = 0;
        // Pinpoint Y-pod offset in inches; replace zero with the measured value.
        public static double PINPOINT_Y_OFFSET_IN = 0;
        // Set true only after replacing the placeholder Pedro gains with tuner results.
        public static boolean FORESIGHT_TUNED = false;
    }

    public static final class Intake {
        private Intake() {}

        // REV configuration name for the intake motor.
        public static final String MOTOR = "intake";
        // Motor power used to pull a scoring element into the robot.
        public static double COLLECT_POWER = 1;
        // Motor power used to return the newest element to the field.
        public static double REVERSE_POWER = -.75;
    }

    public static final class Shooter {
        private Shooter() {}

        // REV configuration name for the single shooter flywheel motor.
        public static final String FLYWHEEL = "flywheel_left";
        // REV configuration name for the motor that feeds elements into the flywheel.
        public static final String FEEDER = "feeder";
        // REV configuration name for the servo that sets the launch angle.
        public static final String HOOD = "hood";
        // Reverses the feeder direction when its physical installation requires it.
        public static boolean FEEDER_REVERSED = false;
        // Starting shooter speed; distance adjustment raises or lowers this target.
        public static double SHOOTER_BASE_TARGET_RPM = 4000;
        // Feeding is allowed only when measured RPM is within this amount of the target.
        public static double SHOOTER_MAX_READY_ERROR_RPM = 160;
        // Encoder pulses produced by one complete shooter motor-shaft revolution.
        public static double SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION = 28;
        // Time the shooter must remain within its allowed RPM error before feeding.
        public static double READY_HOLD_SECONDS = .12;
        // Feeder motor power applied while sending one element into the flywheel.
        public static double FEED_POWER = .85;
        // Duration of one feeder pulse in seconds.
        public static double FEED_SECONDS = .18;
        // Hood servo position used when the shooter is stopped; tune on the robot.
        public static double HOOD_STOW = .16;
        // Hood servo position for the near calibration distance; tune on the robot.
        public static double HOOD_NEAR = .43;
        // Hood servo position for the far calibration distance; tune on the robot.
        public static double HOOD_FAR = .62;
        // Distance in inches represented by the near RPM and hood endpoint.
        public static double NEAR_DISTANCE_IN = 30;
        // Distance in inches represented by the far RPM and hood endpoint.
        public static double FAR_DISTANCE_IN = 84;
    }

    public static final class Vision {
        private Vision() {}

        // REV configuration name for the Limelight 3A.
        public static final String LIMELIGHT = "limelight";
        // Exact neural-detector label used to identify pollen.
        public static final String POLLEN_CLASS = "yellow_pollen";
        // Limelight neural-detector pipeline selected when vision starts.
        public static int POLLEN_PIPELINE = 0;
        // Detections below this confidence, from zero to one, are ignored.
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

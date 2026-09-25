package org.firstinspires.ftc.teamcode.constants;

/** Hardware names and starting values. Never remove a TUNE marker until that value is measured. */
public final class RobotConfig {
    private RobotConfig() {}

    public static final class Drive {
        private Drive() {}

        // REV configuration name for the front-left drive motor.
        public static final String FRONT_LEFT = "front_left_motor";
        // REV configuration name for the front-right drive motor.
        public static final String FRONT_RIGHT = "front_right_motor";
        // REV configuration name for the back-left drive motor.
        public static final String BACK_LEFT = "back_left_motor";
        // REV configuration name for the back-right drive motor.
        public static final String BACK_RIGHT = "back_right_motor";
        // REV configuration name for the goBILDA Pinpoint odometry computer.
        public static final String PINPOINT = "pinpoint";
        // TUNE: increase only if an untouched joystick causes motor movement.
        public static double STICK_DEADBAND = .06;
        // TUNE: maximum TeleOp drive power, from 0.0 to 1.0. Keep at 1.0 for full speed;
        // lower it only if testing shows poor control, wheel slip, or electrical brownouts.
        public static double NORMAL_DRIVE_POWER_LIMIT = 1.0;
        // TUNE: maximum drive output while the driver holds precision mode.
        public static double PRECISION_SCALE = .35;
        // TUNE: sideways distance from the robot's rotation center to the forward-tracking
        // Pinpoint pod, in inches. Left of center is positive; right is negative.
        public static double PINPOINT_X_OFFSET_IN = 0;
        // TUNE: forward/backward distance from the robot's rotation center to the
        // sideways-tracking Pinpoint pod, in inches. Forward is positive; backward is negative.
        public static double PINPOINT_Y_OFFSET_IN = 0;
        // TUNE: maximum Pedro autonomous path speed, from 0.0 to 1.0. Start at 0.35,
        // run ForesightTuner, install its measured values, then raise this gradually toward 1.0.
        // Do not compensate for this safety limit by multiplying controller gains or velocities.
        public static double AUTO_PATH_SPEED_LIMIT = .35;
    }

    public static final class Intake {
        private Intake() {}

        // REV configuration name for the intake motor.
        public static final String MOTOR = "intake";
        // TUNE: motor power used to pull a scoring element into the robot.
        public static double COLLECT_POWER = 1;
        // TUNE: motor power used to return the newest element to the field.
        public static double REVERSE_POWER = -.75;
        // Enable only after sensors keep ElementInventory synchronized with the physical robot.
        // When enabled, collection stops at four elements; reverse remains available for clearing.
        public static boolean ENFORCE_INVENTORY_CAPACITY = false;
    }

    public static final class Shooter {
        private Shooter() {}

        // REV configuration name for the left motor driving the shared shooter flywheel.
        public static final String LEFT_FLYWHEEL = "flywheel_left";
        // REV configuration name for the right motor driving the same shooter flywheel.
        public static final String RIGHT_FLYWHEEL = "flywheel_right";
        // REV configuration name for the motor that feeds elements into the flywheel.
        public static final String FEEDER = "feeder";
        // REV configuration name for the servo that sets the launch angle.
        public static final String HOOD = "hood";
        // VERIFY: Rayansh's configuration used false; confirm both motors assist rather than fight
        // each other before testing them while mechanically coupled to the shared flywheel.
        public static boolean RIGHT_FLYWHEEL_REVERSED = false;
        // Reverses the feeder direction when its physical installation requires it.
        public static boolean FEEDER_REVERSED = false;
        // TUNE: starting shooter speed; distance adjustment raises or lowers this target.
        public static double SHOOTER_BASE_TARGET_RPM = 4000;
        // TUNE: maximum RPM error allowed before the feeder may run.
        public static double SHOOTER_MAX_READY_ERROR_RPM = 160;
        // VERIFY: both intended 1:1 6000-RPM Yellow Jackets produce 28 encoder ticks per revolution.
        // Confirm the installed motor SKUs and 1:1 connection before relying on displayed RPM.
        public static double SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION = 28;
        // TUNE: seconds the shooter must remain within its allowed RPM error before feeding.
        public static double READY_HOLD_SECONDS = .12;
        // TUNE: feeder motor power applied while sending one element into the flywheel.
        public static double FEED_POWER = .85;
        // TUNE: duration of one feeder pulse in seconds.
        public static double FEED_SECONDS = .18;
        // TUNE: safe hood servo position used when the shooter is stopped.
        public static double HOOD_STOW = .16;
        // TUNE: hood servo position for the near calibration distance.
        public static double HOOD_NEAR = .43;
        // TUNE: hood servo position for the far calibration distance.
        public static double HOOD_FAR = .62;
        // TUNE: measured near-shot distance in inches.
        public static double NEAR_DISTANCE_IN = 30;
        // TUNE: measured far-shot distance in inches.
        public static double FAR_DISTANCE_IN = 84;
    }

    public static final class Vision {
        private Vision() {}

        // REV configuration name for the required, launcher-aligned Limelight 3A. Every competition
        // configuration must contain this camera because autonomous Hive aiming depends on it.
        // Keep the original hardware name so adding the second camera does not break the existing
        // Robot Configuration; this original camera is now permanently assigned to Hive aiming.
        public static final String HIVE_LIMELIGHT = "limelight";
        // REV configuration name for the optional Limelight 3A that detects loose pollen. The robot
        // continues with Hive targeting when this name is absent from the active configuration.
        public static final String POLLEN_LIMELIGHT = "pollen_limelight";
        // Exact neural-detector label used to identify pollen.
        public static final String POLLEN_CLASS = "yellow_pollen";
        // VERIFY: neural-detector pipeline stored on the pollen Limelight.
        public static int POLLEN_PIPELINE = 0;
        // VERIFY: use 36h11, 82.55-mm tags and Full 3D on the Hive Limelight. See ROBOT_SETUP.
        public static int HIVE_APRILTAG_PIPELINE = 1;
        // TUNE: detections below this confidence, from zero to one, are ignored.
        public static double MIN_CONFIDENCE = .40;
        // TUNE: horizontal Limelight angle at which the launcher—not necessarily the camera—is
        // aimed at the Hive opening. Zero means the opening should appear on the camera crosshair;
        // use a nonzero value when the camera is mounted sideways from the launcher's firing line.
        // Positive is to the camera's right and negative is to its left. Calibrate at the actual
        // autonomous shooting pose because camera/launcher separation changes the angle by distance.
        public static double HIVE_AIM_BEARING_DEGREES = 0;
        // TUNE: allowed difference between the measured bearing above and the calibrated bearing.
        // At 2 degrees, a target from -2 to +2 degrees is acceptable when the calibration is zero.
        public static double HIVE_AIM_TOLERANCE_DEGREES = 2;
        // TUNE: positive turn gain; code converts camera-right error to Pedro's clockwise turn.
        // Verify wheel directions with an unloaded robot before increasing this gain.
        public static double HIVE_AIM_TURN_POWER_PER_DEGREE = .018;
        // TUNE: maximum autonomous turn power while correcting Hive aim. The default 1.0 does not
        // restrict normal motor power; lower it only if robot testing shows overshoot or lost tags.
        public static double HIVE_AIM_MAX_TURN_POWER = 1;
        // TUNE: maximum camera-result age, including processing latency, in milliseconds.
        public static double MAX_FRAME_AGE_MS = 150;
        // TUNE: maximum disagreement between opening estimates from tags in the same image.
        public static double HIVE_MAX_POSITION_DIFFERENCE_IN = .75;
        // TUNE: maximum rotation disagreement between tag estimates in the same image.
        public static double HIVE_MAX_ROTATION_DIFFERENCE_DEGREES = 3;
        // TUNE: reject tag orientations within this many degrees of appearing sideways/edge-on.
        // This is a confidence margin for an upright-mounted camera, not a Hive mechanical angle.
        public static double HIVE_UPRIGHT_MARGIN_DEGREES = 15;
        // TUNE: optional pause after a feed finishes. Zero adds no unproven delay; increase it only
        // if physical testing shows that an immediate next shot causes a repeatable problem.
        public static double HIVE_POST_FEED_WAIT_SECONDS = 0;
    }

    public static final class Auto {
        private Auto() {}
        // TUNE (disabled): path transitions now wait for Pedro to report completion.
        // public static double PATH_TIMEOUT_SECONDS = 6;
        // TUNE (disabled): the first shot may spin up until SHOOT_CUTOFF_SECONDS.
        // public static double ALIGN_TIMEOUT_SECONDS = 2.5;
        // TUNE: finish shooting here. With the 29-second safety stop, only TWO powered seconds
        // remain for parking at the current setting; verify the route can finish in that time.
        public static double SHOOT_CUTOFF_SECONDS = 27;
        // Final fail-safe: stop every mechanism one second before a 30-second auto ends.
        public static double MATCH_SAFETY_CUTOFF_SECONDS = 29;
    }
}

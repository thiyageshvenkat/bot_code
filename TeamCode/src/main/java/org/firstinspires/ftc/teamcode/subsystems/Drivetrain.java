package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.Range;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.util.opMode.Bot;

/**
 * Controls the robot's drive motors in TeleOp and autonomous.
 *
 * In TeleOp, this class converts joystick input into field-centric or robot-centric movement and
 * applies the selected speed limit. In autonomous, it tells Pedro Pathing which path to follow and
 * reports whether that path is still running.
 */
public class Drivetrain extends SubsystemBase {

    // Pedro's Follower owns both drive output and Pinpoint localization, so this class keeps one
    // shared instance instead of creating separate controllers for TeleOp and autonomous.
    private final Follower follower;
    // Manual robot-centric driving can work without position tracking; autonomous paths cannot.
    private final boolean positionTrackingAvailable;
    // When Pinpoint is absent, the Control Hub IMU can still provide heading for field-oriented
    // TeleOp. It cannot provide the X/Y position required for autonomous paths.
    private final IMU controlHubImu;
    private final boolean fieldHeadingAvailable;

    // Field-centric is the default because stick directions then stay fixed to the field.
    private boolean isRobotCentric = false;
    // The driver can temporarily replace the full-power limit with the precision-mode limit.
    private double speedScale;

    /**
     * Reuses a Follower supplied by the template's Bot runtime when one exists; otherwise creates
     * the BIOBUZZ-configured Follower. Reuse prevents two objects from commanding the same motors
     * or independently tracking the same Pinpoint pose.
     *
     * @param hardwareMap active FTC hardware configuration used when a Follower must be created
     */
    public Drivetrain(HardwareMap hardwareMap) {
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        speedScale = RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT;
        if (Bot.follower != null) {
            follower = Bot.follower;
        } else {
            follower = Constants.create(hardwareMap);
        }
        positionTrackingAvailable = !(follower.localizer instanceof Constants.DummyLocalizer);
        controlHubImu = positionTrackingAvailable ? null : initializeControlHubImu(hardwareMap);
        fieldHeadingAvailable = positionTrackingAvailable || controlHubImu != null;
        isRobotCentric = !fieldHeadingAvailable;
    }

    /** Selects Pedro's manual-drive mode before TeleOp begins sending joystick commands. */
    public void startTeleOp() {
        // With no Pinpoint, the robot's direction at TeleOp initialization becomes field forward.
        if (controlHubImu != null) {
            controlHubImu.resetYaw();
        }
        follower.manual(0, 0, 0);
    }

    /**
     * Stops initialization if autonomous has no real position measurement.
     * A fixed zero pose is sufficient for robot-centric TeleOp motor control, but using it for a
     * path would make Pedro command movement without knowing whether the robot actually moved.
     */
    public void startAuto() {
        requirePositionTracking();
        follower.stop();
    }

    /** Makes forward/strafe relative to the robot's current facing direction. */
    public void driveRobotCentric() { isRobotCentric = true; }

    /** Makes forward/strafe relative to the field, independent of robot heading. */
    public void driveFieldCentric() {
        // Remain robot-centric when neither Pinpoint nor the Control Hub IMU supplies heading.
        if (fieldHeadingAvailable) {
            isRobotCentric = false;
        }
    }

    /** Chooses between the driver-controlled full-speed and precision-speed limits. */
    public void setPrecisionMode(boolean enabled) {
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        if (enabled) {
            speedScale = RobotConfig.Drive.PRECISION_SCALE;
        } else {
            speedScale = RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT;
        }
    }

    /** Applies deadband, fine-control shaping, and the active speed limit to raw stick values. */
    public void setMovement(double forward, double strafe, double turn) {
        applyMovement(shape(forward) * speedScale, shape(strafe) * speedScale,
                shape(turn) * speedScale);
    }

    /** Turns in place at a direct power requested by autonomous vision alignment. */
    public void turnInPlace(double turnPower) {
        follower.manual(0, 0, Range.clip(turnPower, -1.0, 1.0));
    }

    private void applyMovement(double forward, double strafe, double turn) {
        // The tested driver controls define positive strafe and turn as right. Pedro defines those
        // two positive directions as left, so reverse them before Pedro calculates wheel powers.
        double pedroStrafe = -strafe;
        double pedroTurn = -turn;
        if (isRobotCentric) {
            follower.manual(forward, pedroStrafe, pedroTurn);
        } else {
            // Rotate the field direction by the measured robot heading before mixing motor powers.
            follower.manual(ManualDrive.fieldCentric(
                    forward, pedroStrafe, pedroTurn, getFieldHeadingRadians()));
        }
    }

    /** Gives autonomous path control to Pedro until the path completes or stop() is called. */
    public void followPath(Path path) {
        requirePositionTracking();
        follower.follow(path);
    }

    /** Uses Pedro's position controller to resist movement from the robot's current pose. */
    public void holdCurrentPose() {
        requirePositionTracking();
        follower.hold(follower.pose());
    }

    /** Cancels manual/path control and commands the drivetrain to stop. */
    public void stop() {
        follower.stop();
    }

    /** Lets autonomous wait for Pedro to finish the active path or pose hold. */
    public boolean isBusy() {
        return follower.isBusy();
    }

    /** Establishes the field pose from which Pinpoint localization and paths will continue. */
    public void setPose(Pose pose) {
        follower.setPose(pose);
    }

    /** Provides the latest Pinpoint-based field pose for telemetry and autonomous decisions. */
    public Pose getPose() {
        return follower.pose();
    }

    /** Returns whether Pinpoint is available for field-centric driving and autonomous paths. */
    public boolean isPositionTrackingAvailable() {
        return positionTrackingAvailable;
    }

    /** Returns whether field-oriented TeleOp has a usable heading measurement. */
    public boolean isFieldHeadingAvailable() {
        return fieldHeadingAvailable;
    }

    /** Makes the robot's current facing direction the new field-forward direction for TeleOp. */
    public void resetFieldHeading() {
        if (positionTrackingAvailable) {
            follower.setPose(follower.pose().withHeading(0));
        } else if (controlHubImu != null) {
            controlHubImu.resetYaw();
        }
    }

    /**
     * Must run every OpMode loop so Pedro can refresh localization and calculate new motor output.
     */
    @Override
    public void periodic() {
        follower.update();
    }

    /** Prevents autonomous movement from using the fallback pose that never changes. */
    private void requirePositionTracking() {
        if (!positionTrackingAvailable) {
            follower.stop();
            throw new IllegalStateException(
                    "Autonomous movement requires the Pinpoint device named '"
                            + RobotConfig.Drive.PINPOINT + "'.");
        }
    }

    /** Reads heading from Pinpoint when installed, otherwise from the Control Hub IMU. */
    private double getFieldHeadingRadians() {
        if (positionTrackingAvailable) {
            return follower.pose().heading();
        }
        return controlHubImu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
    }

    /** Initializes the Control Hub IMU only when Pinpoint is unavailable. */
    private static IMU initializeControlHubImu(HardwareMap hardwareMap) {
        try {
            IMU imu = hardwareMap.tryGet(IMU.class, RobotConfig.Drive.CONTROL_HUB_IMU);
            if (imu == null) {
                return null;
            }
            RevHubOrientationOnRobot hubOrientation = new RevHubOrientationOnRobot(
                    RobotConfig.Drive.CONTROL_HUB_LOGO_DIRECTION,
                    RobotConfig.Drive.CONTROL_HUB_USB_DIRECTION);
            if (!imu.initialize(new IMU.Parameters(hubOrientation))) {
                return null;
            }
            return imu;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /** Removes stick drift, restores the remaining range, then cubes it for finer low-speed input. */
    private static double shape(double input) {
        double clipped = Range.clip(input, -1.0, 1.0);
        if (Math.abs(clipped) <= RobotConfig.Drive.STICK_DEADBAND) {
            return 0.0;
        }
        double normalized = (Math.abs(clipped) - RobotConfig.Drive.STICK_DEADBAND)
                / (1.0 - RobotConfig.Drive.STICK_DEADBAND);
        return Math.copySign(normalized * normalized * normalized, clipped);
    }
}

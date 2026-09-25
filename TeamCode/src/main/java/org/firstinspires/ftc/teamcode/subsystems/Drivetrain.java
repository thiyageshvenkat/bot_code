package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;
import com.seattlesolvers.solverslib.command.SubsystemBase;

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

    private final Follower follower;

    private boolean isRobotCentric = false;
    private double speedScale = RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT;

    /**
     * Every {@code Subsystem} should take only the {@code HardwareMap} into its constructor.
     * If the subsystem relies on anything else such as a {@code Follower} or another
     * {@code Subsystem}, that logic should be executed within {@code Command}s (except
     * the {@code Drivetrain} class, which uses the {@code Follower}).
     * <p>
     *     The general rule of thumb is that the {@code Subsystem} classes should only take care of
     *     itself. It should be able to be created and function without relying on anything else.
     *     For example, a {@code Turret} should not require a {@code Drivetrain} to be created. The
     *     {@code Turret} class should just focus on being able to move the turret to whatever angle
     *     it is told to.
     * </p>
     * @param hardwareMap the hardware map for getting hardware
     */
    public Drivetrain(HardwareMap hardwareMap) {
        if (Bot.follower != null) {
            follower = Bot.follower;
        } else {
            follower = Constants.create(hardwareMap);
        }
    }

    public void startTeleOp() {
        follower.manual(0, 0, 0);
    }
    public void startAuto() {
        follower.stop();
    }

    public void driveRobotCentric() { isRobotCentric = true; }
    public void driveFieldCentric() { isRobotCentric = false; }

    public void setPrecisionMode(boolean enabled) {
        speedScale = enabled
                ? RobotConfig.Drive.PRECISION_SCALE
                : RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT;
    }

    public void setMovement(double forward, double strafe, double turn) {
        applyMovement(shape(forward) * speedScale, shape(strafe) * speedScale,
                shape(turn) * speedScale);
    }

    private void applyMovement(double forward, double strafe, double turn) {
        if (isRobotCentric) {
            follower.manual(forward, strafe, turn);
        } else {
            follower.manual(ManualDrive.fieldCentric(
                    forward, strafe, turn, follower.pose().heading()));
        }
    }

    public void followPath(Path path) {
        follower.follow(path);
    }

    public void holdCurrentPose() {
        follower.hold(follower.pose());
    }

    public void stop() {
        follower.stop();
    }

    public boolean isBusy() {
        return follower.isBusy();
    }

    public void setPose(Pose pose) {
        follower.setPose(pose);
    }



    public Pose getPose() {
        return follower.pose();
    }

    @Override
    public void periodic() {
        follower.update();
    }

    private static double shape(double input) {
        double clipped = Range.clip(input, -1.0, 1.0);
        if (Math.abs(clipped) <= RobotConfig.Drive.STICK_DEADBAND) return 0.0;
        double normalized = (Math.abs(clipped) - RobotConfig.Drive.STICK_DEADBAND)
                / (1.0 - RobotConfig.Drive.STICK_DEADBAND);
        return Math.copySign(normalized * normalized * normalized, clipped);
    }
}

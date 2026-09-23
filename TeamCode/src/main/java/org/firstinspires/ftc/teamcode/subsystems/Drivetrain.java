package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.util.opMode.Bot;

/**
 * A basic drivetrain class. Most methods are just passthroughs of methods in the Follower class.
 */
public class Drivetrain extends SubsystemBase {

    private final Follower follower;

    private boolean isRobotCentric = false;

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
        follower.startTeleOpDrive(true);
    }
    public void startAuto() {
        follower.activateAllPIDFs();
    }

    public void driveRobotCentric() { isRobotCentric = true; }
    public void driveFieldCentric() { isRobotCentric = false; }

    public void setMovement(double forward, double strafe, double turn) {
        follower.setTeleOpDrive(forward, strafe, turn, isRobotCentric);
    }

    public void followPath(Path path) {
        follower.followPath(path);
    }
    public void followPath(PathChain pathChain) {
        follower.followPath(pathChain);
    }

    public void setPose(Pose pose) {
        follower.setPose(pose);
    }



    public Pose getPose() {
        return follower.getPose();
    }
}

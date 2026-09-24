package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.control.ShotModel;

/** Dual-flywheel hive launcher with speed qualification and timed feeding. */
public final class Shooter extends SubsystemBase {
    public enum State { STOPPED, SPINNING, READY, FEEDING }

    private final DcMotorEx leftFlywheel;
    private final DcMotorEx rightFlywheel;
    private final DcMotorEx feeder;
    private final Servo hood;
    private final ElapsedTime stateTimer = new ElapsedTime();

    private State state = State.STOPPED;
    private double targetVelocity;
    private boolean shotCompleted;

    public Shooter(HardwareMap hardwareMap) {
        leftFlywheel = hardwareMap.get(DcMotorEx.class, RobotConfig.Shooter.LEFT_FLYWHEEL);
        rightFlywheel = hardwareMap.get(DcMotorEx.class, RobotConfig.Shooter.RIGHT_FLYWHEEL);
        feeder = hardwareMap.get(DcMotorEx.class, RobotConfig.Shooter.FEEDER);
        hood = hardwareMap.get(Servo.class, RobotConfig.Shooter.HOOD);

        configureFlywheel(leftFlywheel, DcMotorSimple.Direction.FORWARD);
        configureFlywheel(rightFlywheel, RobotConfig.Shooter.RIGHT_FLYWHEEL_REVERSED
                ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        feeder.setDirection(RobotConfig.Shooter.FEEDER_REVERSED
                ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        feeder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        stop();
    }

    public void prepare(ShotModel.Solution solution) {
        if (solution == null) return;
        targetVelocity = Math.max(0.0, solution.flywheelVelocity);
        hood.setPosition(Range.clip(solution.hoodPosition, 0.0, 1.0));
        leftFlywheel.setVelocity(targetVelocity);
        rightFlywheel.setVelocity(targetVelocity);
        if (state == State.STOPPED) {
            state = State.SPINNING;
            stateTimer.reset();
        }
    }

    public boolean requestFeed() {
        if (state != State.READY) return false;
        state = State.FEEDING;
        stateTimer.reset();
        feeder.setPower(RobotConfig.Shooter.FEED_POWER);
        return true;
    }

    public void stop() {
        leftFlywheel.setPower(0);
        rightFlywheel.setPower(0);
        feeder.setPower(0);
        hood.setPosition(RobotConfig.Shooter.HOOD_STOW);
        targetVelocity = 0.0;
        state = State.STOPPED;
    }

    public State getState() { return state; }
    public double getTargetVelocity() { return targetVelocity; }
    public double getLeftVelocity() { return leftFlywheel.getVelocity(); }
    public double getRightVelocity() { return rightFlywheel.getVelocity(); }

    public boolean consumeShotCompleted() {
        boolean completed = shotCompleted;
        shotCompleted = false;
        return completed;
    }

    @Override
    public void periodic() {
        if (state == State.STOPPED) return;

        boolean atSpeed = Math.abs(leftFlywheel.getVelocity() - targetVelocity)
                <= RobotConfig.Shooter.VELOCITY_TOLERANCE_TPS
                && Math.abs(rightFlywheel.getVelocity() - targetVelocity)
                <= RobotConfig.Shooter.VELOCITY_TOLERANCE_TPS;

        if (state == State.SPINNING) {
            if (!atSpeed) stateTimer.reset();
            else if (stateTimer.seconds() >= RobotConfig.Shooter.READY_HOLD_SECONDS) {
                state = State.READY;
            }
        } else if (state == State.READY && !atSpeed) {
            state = State.SPINNING;
            stateTimer.reset();
        } else if (state == State.FEEDING
                && stateTimer.seconds() >= RobotConfig.Shooter.FEED_SECONDS) {
            feeder.setPower(0);
            shotCompleted = true;
            state = State.SPINNING;
            stateTimer.reset();
        }
    }

    private static void configureFlywheel(DcMotorEx motor, DcMotorSimple.Direction direction) {
        motor.setPower(0);
        motor.setDirection(direction);
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }
}

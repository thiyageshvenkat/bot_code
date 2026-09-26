package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/**
 * Controls both intake motors as one mechanism. Element counting and capacity decisions stay
 * outside this class so future sensors do not complicate simple motor control.
 */
public final class Intake extends SubsystemBase {
    /** Human-readable operating state used by Driver Station telemetry. */
    public enum State { STOPPED, COLLECTING, REVERSING }

    private final DcMotor leftMotor;
    private final DcMotor rightMotor;
    private State state = State.STOPPED;

    /** Configures open-loop power control because intake position and velocity are not targets. */
    public Intake(HardwareMap hardwareMap) {
        leftMotor = hardwareMap.get(DcMotor.class, RobotConfig.Intake.LEFT_MOTOR);
        rightMotor = hardwareMap.get(DcMotor.class, RobotConfig.Intake.RIGHT_MOTOR);
        rightMotor.setDirection(RobotConfig.Intake.RIGHT_MOTOR_REVERSED
                ? DcMotor.Direction.REVERSE : DcMotor.Direction.FORWARD);
        configureMotor(leftMotor);
        configureMotor(rightMotor);
        stop();
    }

    /** Pulls an element into the robot using the configured collection power. */
    public void collect() {
        run(State.COLLECTING, RobotConfig.Intake.COLLECT_POWER);
    }

    /** Runs outward to eject an element or help clear a jam. */
    public void reverse() {
        run(State.REVERSING, RobotConfig.Intake.REVERSE_POWER);
    }

    /** Stops both intake motors and records a matching telemetry state. */
    public void stop() {
        run(State.STOPPED, 0);
    }

    public State getState() {
        return state;
    }

    /** Both motors use power directly because the intake has no position or speed target. */
    private static void configureMotor(DcMotor motor) {
        // Brake reduces roller movement after the operator releases the intake button.
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    // Keeping the state and both motor commands together prevents one side from being left running.
    private void run(State next, double power) {
        state = next;
        leftMotor.setPower(power);
        rightMotor.setPower(power);
    }
}

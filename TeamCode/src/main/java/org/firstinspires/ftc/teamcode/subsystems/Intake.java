package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/**
 * Controls the single intake roller. The mechanism needs only direction and power; element
 * counting and capacity decisions stay outside this class so future sensors can be added without
 * complicating the motor control.
 */
public final class Intake extends SubsystemBase {
    /** Human-readable operating state used by Driver Station telemetry. */
    public enum State { STOPPED, COLLECTING, REVERSING }

    private final DcMotor motor;
    private State state = State.STOPPED;

    /** Configures open-loop power control because intake position and velocity are not targets. */
    public Intake(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotor.class, RobotConfig.Intake.MOTOR);
        // Brake helps prevent the roller from coasting after the operator releases the button.
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
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

    /** Stops the roller and records a matching telemetry state. */
    public void stop() {
        run(State.STOPPED, 0);
    }

    public State getState() {
        return state;
    }

    // Keeping the state change and motor command together prevents telemetry from becoming stale.
    private void run(State next, double power) {
        state = next;
        motor.setPower(power);
    }
}

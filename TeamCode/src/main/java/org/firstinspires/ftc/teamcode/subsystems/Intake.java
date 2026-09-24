package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/** Confirmed single-roller intake. Inventory is corrected by the operator. */
public final class Intake extends SubsystemBase {
    public enum State { STOPPED, COLLECTING, REVERSING }

    private final DcMotor motor;
    private State state = State.STOPPED;

    public Intake(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotor.class, RobotConfig.Intake.MOTOR);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        stop();
    }

    public void collect() {
        run(State.COLLECTING, RobotConfig.Intake.COLLECT_POWER);
    }

    public void reverse() {
        run(State.REVERSING, RobotConfig.Intake.REVERSE_POWER);
    }

    public void stop() {
        run(State.STOPPED, 0);
    }

    public State getState() { return state; }

    private void run(State next, double power) {
        state = next;
        motor.setPower(power);
    }
}

package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/** Lift and gate used to enter scoring elements through the top of a flower. */
public final class FlowerArm extends SubsystemBase {
    public enum State { STOWED, RAISING, READY, DEPOSITING, LOWERING, FAULT }

    private final DcMotorEx lift;
    private final Servo gate;
    private final DigitalChannel bottomLimit;
    private final DigitalChannel topLimit;
    private State state = State.STOWED;

    public FlowerArm(HardwareMap hardwareMap) {
        lift = hardwareMap.get(DcMotorEx.class, RobotConfig.Hardware.FLOWER_LIFT);
        gate = hardwareMap.get(Servo.class, RobotConfig.Hardware.FLOWER_GATE);
        bottomLimit = hardwareMap.tryGet(DigitalChannel.class,
                RobotConfig.Hardware.FLOWER_BOTTOM_LIMIT);
        topLimit = hardwareMap.tryGet(DigitalChannel.class,
                RobotConfig.Hardware.FLOWER_TOP_LIMIT);
        if (bottomLimit != null) bottomLimit.setMode(DigitalChannel.Mode.INPUT);
        if (topLimit != null) topLimit.setMode(DigitalChannel.Mode.INPUT);

        lift.setPower(0);
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        if (bottomPressed()) resetEncoder();
        else lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        closeGate();
    }

    public void raiseToFlower() {
        if (state == State.FAULT) return;
        closeGate();
        lift.setTargetPosition(RobotConfig.Flower.SCORE_TICKS);
        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lift.setPower(Math.abs(RobotConfig.Flower.LIFT_UP_POWER));
        state = State.RAISING;
    }

    public boolean deposit() {
        if (state != State.READY) return false;
        gate.setPosition(RobotConfig.Flower.GATE_OPEN);
        state = State.DEPOSITING;
        return true;
    }

    public void stow() {
        closeGate();
        if (bottomPressed()) {
            lift.setPower(0);
            resetEncoder();
            state = State.STOWED;
            return;
        }
        lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lift.setPower(RobotConfig.Flower.LIFT_DOWN_POWER);
        state = State.LOWERING;
    }

    public void closeGate() {
        gate.setPosition(RobotConfig.Flower.GATE_CLOSED);
        if (state == State.DEPOSITING) state = State.READY;
    }

    public void emergencyStop() {
        lift.setPower(0);
        closeGate();
        state = State.FAULT;
    }

    public State getState() { return state; }
    public boolean atScoreHeight() { return state == State.READY; }
    public int getPositionTicks() { return lift.getCurrentPosition(); }

    @Override
    public void periodic() {
        if (state == State.RAISING) {
            boolean atTarget = Math.abs(lift.getCurrentPosition() - RobotConfig.Flower.SCORE_TICKS)
                    <= RobotConfig.Flower.POSITION_TOLERANCE_TICKS;
            if (topPressed() || atTarget || !lift.isBusy()) {
                lift.setPower(0);
                state = State.READY;
            }
        } else if (state == State.LOWERING
                && (bottomPressed() || lift.getCurrentPosition() <= 0)) {
            lift.setPower(0);
            resetEncoder();
            state = State.STOWED;
        }
    }

    private void resetEncoder() {
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private boolean bottomPressed() {
        return bottomLimit != null && !bottomLimit.getState();
    }

    private boolean topPressed() {
        return topLimit != null && !topLimit.getState();
    }
}

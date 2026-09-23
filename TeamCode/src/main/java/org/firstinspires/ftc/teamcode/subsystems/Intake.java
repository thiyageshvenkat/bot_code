package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.constants.IntakeConfig;
import org.firstinspires.ftc.teamcode.game.ElementInventory;
import org.firstinspires.ftc.teamcode.game.ScoringElement;

/** Rule-aware roller intake with beam counting and automatic jam clearing. */
public final class Intake extends SubsystemBase {
    public enum State { STOPPED, COLLECTING, REVERSING, CLEARING_JAM, FULL }

    private final DcMotorEx motor;
    private final DigitalChannel beam;
    private final ElementInventory inventory;
    private final ElapsedTime stateTimer = new ElapsedTime();

    private State state = State.STOPPED;
    private ScoringElement expectedElement = ScoringElement.POLLEN;
    private boolean previousBeamBlocked;

    public Intake(HardwareMap hardwareMap, ElementInventory inventory) {
        this.inventory = inventory;
        motor = hardwareMap.get(DcMotorEx.class, IntakeConfig.MOTOR);
        motor.setPower(0);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        beam = hardwareMap.tryGet(DigitalChannel.class, IntakeConfig.BEAM);
        if (beam != null) beam.setMode(DigitalChannel.Mode.INPUT);
        previousBeamBlocked = beamBlocked();
    }

    public void expect(ScoringElement element) {
        if (element != null) expectedElement = element;
    }

    public void collect() {
        if (inventory.isFull()) {
            transition(State.FULL);
            return;
        }
        transition(State.COLLECTING);
    }

    public void reverse() {
        transition(State.REVERSING);
    }

    public void stop() {
        transition(State.STOPPED);
    }

    public State getState() { return state; }
    public boolean hasBeamSensor() { return beam != null; }
    public boolean isBeamBlocked() { return beamBlocked(); }
    public double getCurrentAmps() { return motor.getCurrent(CurrentUnit.AMPS); }

    @Override
    public void periodic() {
        boolean blocked = beamBlocked();
        if (state == State.COLLECTING && blocked && !previousBeamBlocked) {
            if (!inventory.tryAdd(expectedElement)) transition(State.FULL);
        }
        previousBeamBlocked = blocked;

        if (state == State.COLLECTING
                && motor.getCurrent(CurrentUnit.AMPS) >= IntakeConfig.JAM_CURRENT_AMPS
                && stateTimer.seconds() >= IntakeConfig.JAM_TIME_SECONDS) {
            transition(State.CLEARING_JAM);
        } else if (state == State.CLEARING_JAM
                && stateTimer.seconds() >= IntakeConfig.CLEAR_TIME_SECONDS) {
            transition(inventory.isFull() ? State.FULL : State.COLLECTING);
        } else if (state == State.FULL && !inventory.isFull()) {
            transition(State.STOPPED);
        }

        switch (state) {
            case COLLECTING:
                motor.setPower(IntakeConfig.COLLECT_POWER);
                break;
            case REVERSING:
            case CLEARING_JAM:
                motor.setPower(IntakeConfig.REVERSE_POWER);
                break;
            default:
                motor.setPower(0);
        }
    }

    private void transition(State next) {
        if (state == next) return;
        state = next;
        stateTimer.reset();
        if (next == State.REVERSING) inventory.rejectNewest();
    }

    private boolean beamBlocked() {
        if (beam == null) return false;
        return IntakeConfig.BEAM_ACTIVE_LOW ? !beam.getState() : beam.getState();
    }
}

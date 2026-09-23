package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.game.ElementInventory;
import org.firstinspires.ftc.teamcode.game.ScoringElement;

/** Servo gate that advances exactly one tracked element to a scoring mechanism. */
public final class Magazine extends SubsystemBase {
    public enum State { CLOSED, FEEDING }

    private final Servo gate;
    private final DigitalChannel exitBeam;
    private final ElementInventory inventory;
    private final ElapsedTime feedTimer = new ElapsedTime();
    private State state = State.CLOSED;
    private boolean previousExitBlocked;
    private boolean feedCompleted;

    public Magazine(HardwareMap hardwareMap, ElementInventory inventory) {
        this.inventory = inventory;
        gate = hardwareMap.get(Servo.class, RobotConfig.Hardware.MAGAZINE_GATE);
        exitBeam = hardwareMap.tryGet(DigitalChannel.class, RobotConfig.Hardware.MAGAZINE_EXIT_BEAM);
        if (exitBeam != null) exitBeam.setMode(DigitalChannel.Mode.INPUT);
        previousExitBlocked = exitBlocked();
        close();
    }

    public boolean requestFeed() {
        if (state == State.FEEDING || inventory.peekNext() == null) return false;
        state = State.FEEDING;
        feedTimer.reset();
        gate.setPosition(RobotConfig.Magazine.GATE_FEED);
        return true;
    }

    public void cancel() {
        close();
    }

    public State getState() { return state; }
    public boolean isFeeding() { return state == State.FEEDING; }
    public ScoringElement nextElement() { return inventory.peekNext(); }
    public boolean hasExitBeam() { return exitBeam != null; }
    public boolean isExitBlocked() { return exitBlocked(); }

    public boolean consumeFeedCompleted() {
        boolean completed = feedCompleted;
        feedCompleted = false;
        return completed;
    }

    @Override
    public void periodic() {
        boolean blocked = exitBlocked();
        boolean crossedExit = exitBeam != null && blocked && !previousExitBlocked;
        previousExitBlocked = blocked;

        if (state == State.FEEDING
                && (crossedExit || feedTimer.seconds() >= RobotConfig.Magazine.FEED_SECONDS)) {
            feedCompleted = true;
            close();
        }
    }

    private void close() {
        state = State.CLOSED;
        gate.setPosition(RobotConfig.Magazine.GATE_CLOSED);
    }

    private boolean exitBlocked() {
        if (exitBeam == null) return false;
        return RobotConfig.Magazine.EXIT_BEAM_ACTIVE_LOW
                ? !exitBeam.getState() : exitBeam.getState();
    }
}

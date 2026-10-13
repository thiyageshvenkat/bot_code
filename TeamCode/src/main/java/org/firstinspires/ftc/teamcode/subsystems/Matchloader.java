package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/** Controls the TeleOp matchloader between its measured stowed and deployed positions. */
public final class Matchloader {
    public enum Command {
        STOWED,
        DEPLOYED
    }

    private final Servo servo;
    private Command command;

    /** Connects the positional-mode Speed Servo and commands the safe starting position. */
    public Matchloader(HardwareMap hardwareMap) {
        servo = hardwareMap.get(Servo.class, RobotConfig.Matchloader.SERVO);
        stow();
    }

    /** Reverses the requested motion immediately, even if the previous motion is unfinished. */
    public void toggle() {
        if (command == Command.STOWED) {
            deploy();
        } else {
            stow();
        }
    }

    /** Raises and retracts the matchloader. */
    public void stow() {
        command = Command.STOWED;
        servo.setPosition(RobotConfig.Matchloader.STOWED_POSITION);
    }

    /** Lowers and extends the matchloader. */
    public void deploy() {
        command = Command.DEPLOYED;
        servo.setPosition(RobotConfig.Matchloader.DEPLOYED_POSITION);
    }

    /** Reports the requested endpoint; a standard FTC servo does not report its measured position. */
    public Command getCommand() {
        return command;
    }
}

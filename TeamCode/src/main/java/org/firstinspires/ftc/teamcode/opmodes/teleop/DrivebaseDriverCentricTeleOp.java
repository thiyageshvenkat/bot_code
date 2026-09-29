package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/** Drivebase-only test where stick directions remain relative to the robot. */
@TeleOp(name = "Drivebase Driver-Centric TeleOp", group = "Testing")
public final class DrivebaseDriverCentricTeleOp extends DrivebaseTeleOp {

    @Override
    protected boolean usesFieldOrientedDrive() {
        return false;
    }
}

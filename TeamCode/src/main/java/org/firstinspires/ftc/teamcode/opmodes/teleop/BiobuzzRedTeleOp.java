package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.game.AllianceColor;

@TeleOp(name = "BIOBUZZ Red Competition", group = "BIOBUZZ")
public final class BiobuzzRedTeleOp extends BiobuzzTeleOpBase {
    public BiobuzzRedTeleOp() {
        super(AllianceColor.RED);
    }
}

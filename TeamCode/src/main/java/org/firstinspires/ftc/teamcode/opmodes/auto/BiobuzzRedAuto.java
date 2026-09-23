package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.game.AllianceColor;

@Autonomous(name = "BIOBUZZ Red Preload + Park", group = "BIOBUZZ")
public final class BiobuzzRedAuto extends BiobuzzAutoBase {
    public BiobuzzRedAuto() {
        super(AllianceColor.RED);
    }
}

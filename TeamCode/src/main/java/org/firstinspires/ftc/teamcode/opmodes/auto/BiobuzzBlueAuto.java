package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.game.ElementInventory.AllianceColor;

@Autonomous(name = "BIOBUZZ Blue Preload + Park", group = "BIOBUZZ")
public final class BiobuzzBlueAuto extends BiobuzzAutoBase {
    public BiobuzzBlueAuto() {
        super(AllianceColor.BLUE);
    }
}

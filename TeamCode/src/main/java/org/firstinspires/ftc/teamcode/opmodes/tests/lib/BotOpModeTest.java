package org.firstinspires.ftc.teamcode.opmodes.tests.lib;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.seattlesolvers.solverslib.command.RunCommand;

import org.firstinspires.ftc.teamcode.util.opMode.BotOpMode;
import org.firstinspires.ftc.teamcode.util.telemetry.TelemetryEx;

@Utility(name = "BotOpMode Test", description = "Test the functionality of the BotOpMode class.")
@Disabled
public class BotOpModeTest extends BotOpMode {

    @Override
    public void initialize() {
        TelemetryEx.getInstance()
                .addData("Status", "Initialized");
//                .forceUpdate();

        schedule(new RunCommand(() -> {
            TelemetryEx.getInstance()
                    .addData("Status", "Running")
                    .addData("Time", getRuntime())
                    .addData("Gamepad 1 Left Stick X from Bot object", bot.driverPad.getLeftX())
                    .addData("Gamepad 1 Left Stick Y from Bot object", bot.driverPad.getLeftY())
                    .requestUpdate();
        }));
    }

}

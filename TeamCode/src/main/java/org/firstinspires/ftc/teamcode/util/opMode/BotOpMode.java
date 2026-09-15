package org.firstinspires.ftc.teamcode.util.opMode;

import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;

import org.firstinspires.ftc.teamcode.opmodes.Config;
import org.firstinspires.ftc.teamcode.util.telemetry.AdvantageScopeHelper;
import org.firstinspires.ftc.teamcode.util.telemetry.TelemetryEx;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.rlog.RLOGServer;

/**
 * An extended version of CommandOpMode that initializes all relevant classes/objects for you. This
 * is a convenience feature for eliminating repetitive code that always occurs between OpModes (e.g.
 * initializing Bot class, TelemetryEx, etc.).
 * <br>
 * However, you must call {@link Bot#specifyOpModeType(Bot.OpModeType)} in the Bot class per OpMode.
 * This is not already done for you.
 * @author lucasvuong
 */
public abstract class BotOpMode extends CommandOpMode {

    protected Bot bot;

    @Override
    public final void runOpMode() throws InterruptedException {
        bot = new Bot(hardwareMap, gamepad1, gamepad2);
        if (Config.USE_TELEMETRY) TelemetryEx.getInstance().init(telemetry, 50);

        if (Config.USE_ADVANTAGE_SCOPE) {
            // run after waitForStart()
            schedule(new InstantCommand(AdvantageScopeHelper::start));
        }

        super.runOpMode();

        Bot.clear();
    }

    @Override
    public final void run() {
        super.run();

        if (Config.USE_TELEMETRY)
            TelemetryEx.getInstance().requestUpdate();
    }
}

package org.firstinspires.ftc.teamcode.util.opMode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.opmodes.Config;
import org.firstinspires.ftc.teamcode.util.telemetry.AdvantageScopeHelper;
import org.firstinspires.ftc.teamcode.util.telemetry.TelemetryEx;

/**
 * Similar to BotOpMode but without the Bot class. Use this for OpModes that don't need the Bot
 * object.
 * <br>
 * You need to call {@link AdvantageScopeHelper#start()} in your {@link #run()} method after
 * {@link #waitForStart()} if you want to use AdvantageScope.
 *
 * @author lucasvuong
 */
public abstract class LinearOpModeEx extends LinearOpMode {


    /**
     * Don't use this method. Use {@code run()} instead. This method is only here to initialize
     * TelemetryEx.
     */
    @Override
    public final void runOpMode() {
        if (Config.USE_TELEMETRY) TelemetryEx.getInstance().init(telemetry, 50);

        run();

        AdvantageScopeHelper.end();
    }

    /**
     * The same as {@code runOpMode()} in {@code LinearOpMode}. Use this instead.
     */
    public abstract void run();
}

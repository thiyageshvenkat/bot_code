package org.firstinspires.ftc.teamcode.util.opMode;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.Robot;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.util.telemetry.AdvantageScopeHelper;

public class Bot extends Robot {

    public enum OpModeType { TELEOP, AUTO }

    /**
     * The HardwareMap for the bot.
     * Should be passed into subsystems, thus not static.
     */
    public HardwareMap hardwareMap;
    public static Follower follower;

    public GamepadEx driverPad;
    public GamepadEx opPad;


    public Drivetrain drivetrain;


    public Bot(HardwareMap hardwareMap, Gamepad gamepad1, Gamepad gamepad2) {
        this.hardwareMap = hardwareMap;
        Bot.follower = Constants.create(hardwareMap);

        this.driverPad = new GamepadEx(gamepad1);
        this.opPad = new GamepadEx(gamepad2);

        // Cache clearing is already done for you by SolversLib's CommandOpMode.
        setBulkReading(hardwareMap, LynxModule.BulkCachingMode.MANUAL);

        initSubsystems();
    }

    /**
     * A method to initialize all subsystems on the bot.
     * For OpMode-specific initializations, use {@code void specifyOpModeType(OpModeType)}.
     */
    private void initSubsystems() {
        drivetrain = new Drivetrain(hardwareMap);
    }

    /**
     * An OpMode-specific init. Call this at the beginning of each OpMode.
     * @param opModeType specifies which OpMode is being run
     */
    public void specifyOpModeType(OpModeType opModeType) {
        if (opModeType == OpModeType.TELEOP) {
            drivetrain.startTeleOp();
        } else { // opModeType == OpModeType.AUTO
            drivetrain.startAuto();
        }
    }


    /**
     * Clears the static fields of the Bot class.
     * Must be called at the end of every OpMode, which is already done for you in {@code BotOpMode}.
     */
    public static void clear() {
        follower = null;
        AdvantageScopeHelper.end();
    }

}
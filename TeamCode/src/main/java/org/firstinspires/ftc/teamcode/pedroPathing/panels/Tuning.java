package org.firstinspires.ftc.teamcode.pedroPathing.panels;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.Tests;

/** Procedures shown on Pedro 3's AutoTune webpage at http://192.168.43.1:10158. */
public class Tuning {

    /** Identifies the four drive-motor configuration names and physical directions. */
    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

    /** Measures the Pinpoint pod directions and their physical offsets from the robot center. */
    @Tuner
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
    }

    /** Measures how this drivetrain accelerates, brakes, and corrects path error. */
    @Tuner
    public static Procedure foresightTuner() {
        return new ForesightTuner(
                hardwareMap -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig),
                hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig));
    }

    /** Verifies the combined drivetrain, Pinpoint, and Foresight configuration. */
    @Tuner
    public static Procedure tests() {
        return new Tests(
                hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig),
                hardwareMap -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig),
                () -> new Foresight(Constants.foresightConfig));
    }
}

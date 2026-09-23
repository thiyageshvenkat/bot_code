package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/** Pedro Pathing 3 assembly for the BIOBUZZ mecanum chassis and Pinpoint. */
public final class Constants {
    private Constants() {}

    public static final MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(RobotConfig.Hardware.FRONT_LEFT);
        c.frontRightName.set(RobotConfig.Hardware.FRONT_RIGHT);
        c.backLeftName.set(RobotConfig.Hardware.BACK_LEFT);
        c.backRightName.set(RobotConfig.Hardware.BACK_RIGHT);
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.manualBrakeMode.set(true);
    });

    public static final PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set(RobotConfig.Hardware.PINPOINT);
        c.xPodOffset.set(RobotConfig.Drive.PINPOINT_X_OFFSET_IN);
        c.yPodOffset.set(RobotConfig.Drive.PINPOINT_Y_OFFSET_IN);
        c.offsetUnits.set(DistanceUnit.INCH);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.resetMode.set(PinpointLocalizer.ResetMode.RESET_AND_RECALIBRATE_IMU);
    });

    /** Conservative placeholders; replace with ForesightTuner output before full-speed paths. */
    public static final ForesightConfig foresightConfig = new ForesightConfig(c -> {
        c.forwardTranslational.set(Controller.proportional(0.10));
        c.strafeTranslational.set(Controller.proportional(0.10));
        c.headingFeedback.set(Controller.proportional(1.0));
        c.coast.set(Controller.proportionalFeedforward(0.01));
        c.brake.set(Controller.proportionalFeedforward(0.01));
        c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05, 0.005));
        c.linearBrakeCoefficients.set(Matrix.diag(0.10, 0.10));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.001, 0.001));
        c.maxAchievableForwardVelocity.set(50.0);
        c.maxAchievableStrafeVelocity.set(40.0);
        c.naturalForwardDeceleration.set(80.0);
        c.naturalStrafeDeceleration.set(80.0);
        c.maxPathSpeed.set(RobotConfig.Drive.FORESIGHT_TUNED ? 1.0 : 0.35);
    });

    public static Follower create(HardwareMap hardwareMap) {
        return new Follower(
                new PinpointLocalizer(hardwareMap, localizerConfig),
                new Mecanum(hardwareMap, drivetrainConfig),
                new Foresight(foresightConfig));
    }
}

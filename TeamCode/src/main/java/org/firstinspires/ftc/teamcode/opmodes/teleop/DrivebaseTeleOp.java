package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/**
 * Minimal TeleOp OpMode for testing just a 4-motor drivebase chassis.
 * Does not require intake, shooter, vision, or Pinpoint hardware in the configuration.
 */
@TeleOp(name = "Drivebase Only TeleOp", group = "Testing")
public class DrivebaseTeleOp extends OpMode {
    private Drivetrain drivetrain;

    @Override
    public void init() {
        drivetrain = new Drivetrain(hardwareMap);
        drivetrain.startTeleOp();
        telemetry.addData("Drive mode", drivetrain.isPositionTrackingAvailable()
                ? "Field-centric (Pinpoint detected)"
                : "Robot-centric (no Pinpoint)");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Left stick translates, right stick turns, right trigger enables precision speed
        drivetrain.setPrecisionMode(gamepad1.right_trigger > 0.4);
        drivetrain.setMovement(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        drivetrain.periodic();

        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.update();
    }

    @Override
    public void stop() {
        drivetrain.stop();
    }
}

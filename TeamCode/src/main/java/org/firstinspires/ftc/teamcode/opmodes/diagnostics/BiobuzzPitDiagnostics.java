package org.firstinspires.ftc.teamcode.opmodes.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.control.ShotModel;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.PollenVision;

/** Hold-to-run checks for the hardware documented by the existing robot code. */
@TeleOp(name = "BIOBUZZ Pit Diagnostics", group = "BIOBUZZ Diagnostics")
public final class BiobuzzPitDiagnostics extends OpMode {
    private Drivetrain drivetrain;
    private Superstructure superstructure;

    @Override
    public void init() {
        drivetrain = new Drivetrain(hardwareMap);
        drivetrain.startTeleOp();
        drivetrain.driveRobotCentric();
        drivetrain.setPrecisionMode(true);
        superstructure = new Superstructure(hardwareMap, AllianceColor.RED);
        telemetry.addLine("PIT ONLY: lift robot and clear intake/launcher");
        telemetry.addLine("Hold gamepad2 START to power hardware");
    }

    @Override
    public void loop() {
        boolean armed = gamepad2.start;
        drivetrain.setMovement(armed ? -gamepad1.left_stick_y : 0,
                armed ? gamepad1.left_stick_x : 0,
                armed ? gamepad1.right_stick_x : 0);
        if (armed && gamepad2.a) superstructure.intake.collect();
        else if (armed && gamepad2.y) superstructure.intake.reverse();
        else superstructure.intake.stop();
        if (armed && gamepad2.right_bumper) {
            superstructure.shooter.prepare(ShotModel.forDistance(30.0));
        } else superstructure.shooter.stop();

        drivetrain.periodic();
        superstructure.periodic();
        PollenVision.Target target = superstructure.vision.bestPollen();
        telemetry.addData("ARMED", armed);
        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.addData("Intake", superstructure.intake.getState());
        telemetry.addData("Flywheels", "%.0f / %.0f TPS",
                superstructure.shooter.getLeftVelocity(),
                superstructure.shooter.getRightVelocity());
        telemetry.addData("Limelight", superstructure.vision.isConnected());
        telemetry.addData("Pollen", target == null ? "not visible"
                : String.format("%.1f deg, %.2f%%", target.bearingDegrees, target.areaPercent));
        telemetry.update();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        superstructure.close();
    }
}

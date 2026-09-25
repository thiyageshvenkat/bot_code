package org.firstinspires.ftc.teamcode.opmodes.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.control.ShotModel;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.BiobuzzVision;

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
        double forward = 0;
        double strafe = 0;
        double turn = 0;
        if (armed) {
            forward = -gamepad1.left_stick_y;
            strafe = gamepad1.left_stick_x;
            turn = gamepad1.right_stick_x;
        }
        drivetrain.setMovement(forward, strafe, turn);

        if (armed && gamepad2.a) {
            superstructure.intake.collect();
        } else if (armed && gamepad2.y) {
            superstructure.intake.reverse();
        } else {
            superstructure.intake.stop();
        }
        if (armed && gamepad2.right_bumper) {
            superstructure.vision.useHiveAprilTagPipeline();
            superstructure.shooter.prepare(ShotModel.forDistance(30.0));
        } else {
            superstructure.vision.usePollenPipeline();
            superstructure.shooter.stop();
        }

        drivetrain.periodic();
        superstructure.periodic();
        BiobuzzVision.PollenTarget pollenTarget = superstructure.vision.bestPollen();
        BiobuzzVision.HiveTarget hiveTarget = superstructure.vision.upwardHiveTarget(
                AllianceColor.RED);
        telemetry.addData("ARMED", armed);
        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.addData("Intake", superstructure.intake.getState());
        telemetry.addData("Flywheel motors", "left: %.0f RPM | right: %.0f RPM",
                superstructure.shooter.getLeftSpeedRpm(),
                superstructure.shooter.getRightSpeedRpm());
        telemetry.addData("Limelight", superstructure.vision.isConnected());
        telemetry.addData("Vision mode", superstructure.vision.getMode());
        if (superstructure.vision.getMode() == BiobuzzVision.Mode.POLLEN) {
            if (pollenTarget == null) {
                telemetry.addData("Pollen", "not visible");
            } else {
                telemetry.addData("Pollen", "%.1f deg, %.2f%%",
                        pollenTarget.bearingDegrees, pollenTarget.areaPercent);
            }
        } else if (hiveTarget == null) {
            telemetry.addData("Red Hive opening estimate", "no usable fresh upright target");
        } else {
            telemetry.addData("Red Hive opening estimate", "%s | %.1f deg | %d tags",
                    hiveTarget.cell, hiveTarget.bearingDegrees, hiveTarget.visibleTagCount);
        }
        telemetry.update();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        superstructure.close();
    }
}

package org.firstinspires.ftc.teamcode.opmodes.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.control.ShotModel;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.HiveObservation;

/** Deliberately hold-to-run pit checks. Keep the robot clear and off the field. */
@TeleOp(name = "BIOBUZZ Pit Diagnostics", group = "BIOBUZZ Diagnostics")
public final class BiobuzzPitDiagnostics extends OpMode {
    private Drivetrain drivetrain;
    private Superstructure superstructure;
    private boolean previouslyArmed;

    @Override
    public void init() {
        drivetrain = new Drivetrain(hardwareMap);
        drivetrain.startTeleOp();
        drivetrain.driveRobotCentric();
        drivetrain.setPrecisionMode(true);
        superstructure = new Superstructure(hardwareMap, AllianceColor.BLUE);
        telemetry.addLine("PIT ONLY: lift robot and clear every mechanism");
        telemetry.addLine("All powered checks require holding gamepad2 START");
    }

    @Override
    public void loop() {
        boolean armed = gamepad2.start;
        if (previouslyArmed && !armed) superstructure.flower.emergencyStop();
        previouslyArmed = armed;
        drivetrain.setMovement(armed ? -gamepad1.left_stick_y : 0,
                armed ? gamepad1.left_stick_x : 0,
                armed ? gamepad1.right_stick_x : 0);

        if (!armed || gamepad2.b) {
            superstructure.intake.stop();
            superstructure.shooter.stop();
        } else {
            if (gamepad2.a) superstructure.intake.collect();
            else if (gamepad2.y) superstructure.intake.reverse();
            else superstructure.intake.stop();

            if (gamepad2.right_bumper) {
                superstructure.shooter.prepare(ShotModel.forDistance(30.0));
            } else superstructure.shooter.stop();
            if (gamepad2.dpad_up) superstructure.flower.raiseToFlower();
            if (gamepad2.dpad_down) superstructure.flower.stow();
        }

        drivetrain.periodic();
        superstructure.periodic();
        publishTelemetry(armed);
    }

    private void publishTelemetry(boolean armed) {
        HiveObservation target = superstructure.vision.closestCell();
        telemetry.addData("ARMED (hold START)", armed);
        telemetry.addData("Drive pose", drivetrain.getPose());
        telemetry.addData("Intake", "%s | beam %s | %.1f A",
                superstructure.intake.getState(),
                sensor(superstructure.intake.hasBeamSensor(),
                        superstructure.intake.isBeamBlocked()),
                superstructure.intake.getCurrentAmps());
        telemetry.addData("Magazine exit", sensor(superstructure.magazine.hasExitBeam(),
                superstructure.magazine.isExitBlocked()));
        telemetry.addData("Flywheels", "%s | %.0f / %.0f TPS",
                superstructure.shooter.getState(),
                superstructure.shooter.getLeftVelocity(),
                superstructure.shooter.getRightVelocity());
        telemetry.addData("Flower", "%s | %d ticks", superstructure.flower.getState(),
                superstructure.flower.getPositionTicks());
        telemetry.addData("Bottom limit", sensor(superstructure.flower.hasBottomLimit(),
                superstructure.flower.isBottomPressed()));
        telemetry.addData("Top limit", sensor(superstructure.flower.hasTopLimit(),
                superstructure.flower.isTopPressed()));
        telemetry.addData("Hive tag", target == null ? "not visible"
                : String.format("%.1f in | %.1f deg", target.rangeInches, target.bearingDegrees));
        telemetry.update();
    }

    private static String sensor(boolean installed, boolean active) {
        return installed ? (active ? "ACTIVE" : "clear") : "not configured";
    }

    @Override
    public void stop() {
        drivetrain.stop();
        superstructure.close();
    }
}

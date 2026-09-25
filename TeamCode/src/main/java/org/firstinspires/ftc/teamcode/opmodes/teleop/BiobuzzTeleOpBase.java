package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.game.ScoringElement;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.PollenVision;

/** Shared competition controls; registered red and blue variants provide alliance-safe nectar handling. */
abstract class BiobuzzTeleOpBase extends OpMode {
    // The concrete red/blue OpMode supplies this so nectar is recorded for the correct alliance.
    private final AllianceColor alliance;
    protected Drivetrain drivetrain;
    protected Superstructure superstructure;

    // Previous button states make each press trigger once instead of once per control-loop cycle.
    private boolean previousShoot;
    private boolean previousInventoryAdd;
    private boolean previousInventoryRemove;
    private boolean previousNectarAdd;

    BiobuzzTeleOpBase(AllianceColor alliance) {
        this.alliance = alliance;
    }

    @Override
    public void init() {
        // FTC calls init once after the driver selects this OpMode.
        drivetrain = new Drivetrain(hardwareMap);
        drivetrain.startTeleOp();
        superstructure = new Superstructure(hardwareMap, alliance);
        superstructure.seedPreloadPollen();
        telemetry.addLine("BIOBUZZ initialized");
        telemetry.addLine("During INIT: operator dpad up/down corrects preload inventory");
    }

    @Override
    public void init_loop() {
        // Let the operator correct the preload count before the match starts.
        updateInventory(false);
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Starting inventory", superstructure.inventory.snapshot());
        telemetry.update();
    }

    @Override
    public void loop() {
        // Driver controls: left stick translates, right stick turns, and right trigger slows the drive.
        drivetrain.setPrecisionMode(gamepad1.right_trigger > 0.4);
        drivetrain.setMovement(-gamepad1.left_stick_y, gamepad1.left_stick_x,
                gamepad1.right_stick_x);

        // Operator intake controls: A/X collect, Y ejects, and releasing them stops the intake.
        if (gamepad2.a || gamepad2.x) {
            superstructure.intake.collect();
        } else if (gamepad2.y) {
            superstructure.intake.reverse();
        } else {
            superstructure.intake.stop();
        }

        // Hold the bumper to spin up; each trigger press requests one feed cycle.
        if (gamepad2.right_bumper) {
            superstructure.prepareHiveShot();
        }
        boolean shoot = gamepad2.right_trigger > 0.5;
        if (shoot && !previousShoot) {
            superstructure.queueHiveShot();
        }
        previousShoot = shoot;

        updateInventory(true);

        drivetrain.periodic();
        superstructure.periodic();
        publishTelemetry();
    }

    private void updateInventory(boolean allowNectar) {
        // D-pad edits are manual bookkeeping until physical element sensors are installed.
        boolean pollen = gamepad2.dpad_up;
        boolean nectar = allowNectar && gamepad2.dpad_right;
        boolean remove = gamepad2.dpad_down;

        if (pollen && !previousInventoryAdd) {
            superstructure.inventory.tryAdd(ScoringElement.POLLEN);
        }
        if (nectar && !previousNectarAdd) {
            superstructure.inventory.tryAdd(getAllianceNectar());
        }
        if (remove && !previousInventoryRemove) {
            superstructure.inventory.rejectNewest();
        }

        previousInventoryAdd = pollen;
        previousNectarAdd = nectar;
        previousInventoryRemove = remove;
    }

    /** Returns the nectar type that this alliance is allowed to track. */
    private ScoringElement getAllianceNectar() {
        if (alliance == AllianceColor.RED) {
            return ScoringElement.RED_NECTAR;
        }
        return ScoringElement.BLUE_NECTAR;
    }

    private void publishTelemetry() {
        // Show the values most useful to the drive team during a match.
        PollenVision.Target target = superstructure.vision.bestPollen();
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Inventory", superstructure.inventory.snapshot());
        telemetry.addData("Intake", superstructure.intake.getState());
        telemetry.addData("Shooter RPM", "%s | actual: %.0f | target: %.0f",
                superstructure.shooter.getState(), superstructure.shooter.getSpeedRpm(),
                superstructure.shooter.getTargetRpm());
        telemetry.addData("Limelight", superstructure.vision.isConnected());
        if (target == null) {
            telemetry.addData("Pollen", "not visible");
        } else {
            telemetry.addData("Pollen", "%.1f deg, %.2f%%",
                    target.bearingDegrees, target.areaPercent);
        }
        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.update();
    }

    @Override
    public void stop() {
        // Leave every motor stopped and release the Limelight when FTC ends the OpMode.
        drivetrain.stop();
        superstructure.close();
    }
}

package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.game.ScoringElement;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.PollenVision;

/** Shared competition controls; registered red and blue variants provide alliance-safe nectar handling. */
abstract class BiobuzzTeleOpBase extends OpMode {
    // Manual counting cannot reliably follow a fast intake and shooter, so it stays disabled.
    // Keep this false when sensors later update ElementInventory automatically.
    private static final boolean MANUAL_INVENTORY_CONTROLS_ENABLED = false;

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
        if (MANUAL_INVENTORY_CONTROLS_ENABLED) {
            telemetry.addLine("During INIT: operator dpad up/down corrects preload inventory");
        } else {
            telemetry.addLine("Manual inventory controls disabled; sensor tracking pending");
        }
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
        // Capacity enforcement remains off until sensors make the software inventory trustworthy.
        if (gamepad2.a || gamepad2.x) {
            if (isIntakeCapacityLocked()) {
                superstructure.intake.stop();
            } else {
                superstructure.intake.collect();
            }
        } else if (gamepad2.y) {
            // Reverse must remain available even when full so the operator can clear an element.
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

    /** Returns true when sensor-backed inventory says collection must stop at capacity. */
    private boolean isIntakeCapacityLocked() {
        return RobotConfig.Intake.ENFORCE_INVENTORY_CAPACITY
                && superstructure.inventory.isFull();
    }

    /**
     * Applies at most one manual inventory edit for each new D-pad press.
     * Holding a button does not repeatedly edit the inventory because the current button states
     * are compared with their states from the previous control-loop cycle.
     *
     * @param allowNectar false during INIT, when only the pollen preload should be corrected;
     *                    true after the match starts
     */
    private void updateInventory(boolean allowNectar) {
        if (!MANUAL_INVENTORY_CONTROLS_ENABLED) {
            return;
        }

        // Read the three manual bookkeeping controls once during this loop cycle.
        boolean pollen = gamepad2.dpad_up;
        boolean nectar = allowNectar && gamepad2.dpad_right;
        boolean remove = gamepad2.dpad_down;

        // A true current state and false previous state means the button was just pressed.
        if (pollen && !previousInventoryAdd) {
            superstructure.inventory.tryAdd(ScoringElement.POLLEN);
        }
        if (nectar && !previousNectarAdd) {
            superstructure.inventory.tryAdd(getAllianceNectar());
        }
        if (remove && !previousInventoryRemove) {
            superstructure.inventory.rejectNewest();
        }

        // Save the states so a held button is not treated as another press on the next loop.
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
        telemetry.addData("Intake capacity lockout", isIntakeCapacityLocked());
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

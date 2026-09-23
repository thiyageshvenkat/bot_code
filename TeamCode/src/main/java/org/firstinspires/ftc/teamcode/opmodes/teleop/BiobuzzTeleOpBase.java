package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.control.MatchClock;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.game.ScoringElement;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.HiveObservation;

/** Shared competition controls; registered red and blue variants provide alliance-safe nectar handling. */
abstract class BiobuzzTeleOpBase extends OpMode {
    private final AllianceColor alliance;
    protected Drivetrain drivetrain;
    protected Superstructure superstructure;
    private final MatchClock matchClock = new MatchClock();

    private boolean previousShoot;
    private boolean previousFlowerStage;
    private boolean previousFlowerDeposit;
    private boolean previousInventoryAdd;
    private boolean previousInventoryRemove;

    BiobuzzTeleOpBase(AllianceColor alliance) {
        this.alliance = alliance;
    }

    @Override
    public void init() {
        drivetrain = new Drivetrain(hardwareMap);
        drivetrain.startTeleOp();
        superstructure = new Superstructure(hardwareMap, alliance);
        superstructure.seedPreloadPollen();
        telemetry.addLine("BIOBUZZ initialized");
        telemetry.addLine("During INIT: operator dpad up/down corrects preload inventory");
    }

    @Override
    public void init_loop() {
        boolean add = gamepad2.dpad_up;
        boolean remove = gamepad2.dpad_down;
        if (add && !previousInventoryAdd) {
            superstructure.inventory.tryAdd(ScoringElement.POLLEN);
        }
        if (remove && !previousInventoryRemove) {
            superstructure.inventory.releaseNext();
        }
        previousInventoryAdd = add;
        previousInventoryRemove = remove;
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Starting inventory", superstructure.inventory.snapshot());
        telemetry.update();
    }

    @Override
    public void start() {
        matchClock.startTeleop();
    }

    @Override
    public void loop() {
        double forward = -gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double turn = gamepad1.right_stick_x;
        drivetrain.setPrecisionMode(gamepad1.right_trigger > 0.4
                || superstructure.flower.getState() != org.firstinspires.ftc.teamcode.subsystems.FlowerArm.State.STOWED);
        if (gamepad1.left_bumper) {
            drivetrain.setMovementWithRawTurn(forward, strafe,
                    superstructure.vision.aimTurnPower());
        } else {
            drivetrain.setMovement(forward, strafe, turn);
        }

        if (gamepad2.a) {
            superstructure.intake.expect(ScoringElement.POLLEN);
            superstructure.intake.collect();
        } else if (gamepad2.x) {
            superstructure.intake.expect(alliance == AllianceColor.RED
                    ? ScoringElement.RED_NECTAR : ScoringElement.BLUE_NECTAR);
            superstructure.intake.collect();
        } else if (gamepad2.y) {
            superstructure.intake.reverse();
        } else if (gamepad2.b) {
            superstructure.intake.stop();
        }

        if (gamepad2.right_bumper) superstructure.prepareHiveShot();
        boolean shoot = gamepad2.right_trigger > 0.5;
        if (shoot && !previousShoot) superstructure.queueHiveShot(true);
        previousShoot = shoot;

        boolean flowerStage = gamepad2.dpad_up;
        if (flowerStage && !previousFlowerStage) {
            superstructure.queueFlowerTransfer(matchClock.elapsedSeconds());
        }
        previousFlowerStage = flowerStage;

        boolean flowerDeposit = gamepad2.dpad_down;
        if (flowerDeposit && !previousFlowerDeposit) {
            superstructure.depositFlower(matchClock.elapsedSeconds());
        }
        previousFlowerDeposit = flowerDeposit;
        if (gamepad2.left_bumper) superstructure.flower.stow();

        drivetrain.periodic();
        superstructure.periodic();
        publishTelemetry();
    }

    private void publishTelemetry() {
        HiveObservation target = superstructure.vision.closestCell();
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Match remaining", "%.1f", matchClock.remainingSeconds());
        telemetry.addData("Flower unlocked", matchClock.flowerUnlocked());
        telemetry.addData("Inventory", superstructure.inventory.snapshot());
        telemetry.addData("Route", superstructure.getRoute());
        telemetry.addData("Intake", superstructure.intake.getState());
        telemetry.addData("Shooter", "%s %.0f/%.0f -> %.0f",
                superstructure.shooter.getState(), superstructure.shooter.getLeftVelocity(),
                superstructure.shooter.getRightVelocity(), superstructure.shooter.getTargetVelocity());
        telemetry.addData("Flower", "%s @ %d", superstructure.flower.getState(),
                superstructure.flower.getPositionTicks());
        telemetry.addData("Hive target", target == null ? "not visible"
                : String.format("%.1f in, %.1f deg", target.rangeInches, target.bearingDegrees));
        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.update();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        superstructure.close();
    }
}

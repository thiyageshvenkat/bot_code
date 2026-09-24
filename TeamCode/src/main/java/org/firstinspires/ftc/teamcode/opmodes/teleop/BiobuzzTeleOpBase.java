package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.ElementInventory.AllianceColor;
import org.firstinspires.ftc.teamcode.game.ElementInventory.ScoringElement;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.PollenVision;

/** Shared competition controls; registered red and blue variants provide alliance-safe nectar handling. */
abstract class BiobuzzTeleOpBase extends OpMode {
    private final AllianceColor alliance;
    protected Drivetrain drivetrain;
    protected Superstructure superstructure;
    private boolean previousShoot;
    private boolean previousInventoryAdd;
    private boolean previousInventoryRemove;
    private boolean previousNectarAdd;

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
        updateInventory(false);
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Starting inventory", superstructure.inventory.snapshot());
        telemetry.update();
    }

    @Override
    public void loop() {
        double forward = -gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double turn = gamepad1.right_stick_x;
        drivetrain.setPrecisionMode(gamepad1.right_trigger > 0.4);
        drivetrain.setMovement(forward, strafe, turn);

        if (gamepad2.a || gamepad2.x) superstructure.intake.collect();
        else if (gamepad2.y) {
            superstructure.intake.reverse();
        } else superstructure.intake.stop();

        if (gamepad2.right_bumper) superstructure.prepareHiveShot();
        boolean shoot = gamepad2.right_trigger > 0.5;
        if (shoot && !previousShoot) superstructure.queueHiveShot();
        previousShoot = shoot;

        updateInventory(true);

        drivetrain.periodic();
        superstructure.periodic();
        publishTelemetry();
    }

    private void updateInventory(boolean allowNectar) {
        boolean pollen = gamepad2.dpad_up;
        boolean nectar = allowNectar && gamepad2.dpad_right;
        boolean remove = gamepad2.dpad_down;
        if (pollen && !previousInventoryAdd) superstructure.inventory.tryAdd(ScoringElement.POLLEN);
        if (nectar && !previousNectarAdd) superstructure.inventory.tryAdd(alliance == AllianceColor.RED
                ? ScoringElement.RED_NECTAR : ScoringElement.BLUE_NECTAR);
        if (remove && !previousInventoryRemove) superstructure.inventory.rejectNewest();
        previousInventoryAdd = pollen;
        previousNectarAdd = nectar;
        previousInventoryRemove = remove;
    }

    private void publishTelemetry() {
        PollenVision.Target target = superstructure.vision.bestPollen();
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Inventory", superstructure.inventory.snapshot());
        telemetry.addData("Intake", superstructure.intake.getState());
        telemetry.addData("Shooter", "%s %.0f/%.0f -> %.0f",
                superstructure.shooter.getState(), superstructure.shooter.getLeftVelocity(),
                superstructure.shooter.getRightVelocity(), superstructure.shooter.getTargetVelocity());
        telemetry.addData("Limelight", superstructure.vision.isConnected());
        telemetry.addData("Pollen", target == null ? "not visible"
                : String.format("%.1f deg, %.2f%%", target.bearingDegrees, target.areaPercent));
        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.update();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        superstructure.close();
    }
}

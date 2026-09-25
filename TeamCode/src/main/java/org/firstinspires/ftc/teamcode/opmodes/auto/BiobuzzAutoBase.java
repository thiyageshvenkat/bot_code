package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.auto.BiobuzzAutoPlan;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.BiobuzzVision;

/** Shared, timeout-protected preload scoring autonomous. */
abstract class BiobuzzAutoBase extends OpMode {
    private enum State { DRIVE_TO_SHOT, ALIGN_AND_SHOOT, DRIVE_TO_PARK, DONE }

    private final AllianceColor alliance;
    private final ElapsedTime matchTimer = new ElapsedTime();
    private final ElapsedTime alignedCellTimer = new ElapsedTime();
    private Drivetrain drivetrain;
    private Superstructure superstructure;
    private BiobuzzAutoPlan plan;
    private State state;
    private int shotsRequested;
    private BiobuzzVision.HiveCell lastAlignedCell;

    BiobuzzAutoBase(AllianceColor alliance) {
        this.alliance = alliance;
    }

    @Override
    public void init() {
        plan = BiobuzzAutoPlan.forAlliance(alliance);
        drivetrain = new Drivetrain(hardwareMap);
        drivetrain.startAuto();
        drivetrain.setPose(plan.start);
        superstructure = new Superstructure(hardwareMap, alliance);
        superstructure.seedPreloadPollen();
        // Autonomous targets the official tags under the upward-facing Hive Cell, not loose pollen.
        superstructure.vision.useHiveAprilTagPipeline();
        telemetry.addData("BIOBUZZ auto", alliance);
        telemetry.addLine("Check start pose, visible own-Hive tags, and clear park route");
    }

    @Override
    public void init_loop() {
        telemetry.addData("Start pose", plan.start);
        telemetry.addData("Limelight connected", superstructure.vision.isConnected());
        BiobuzzVision.HiveTarget target = superstructure.vision.upwardHiveTarget(alliance);
        if (target == null) {
            telemetry.addData("Upward Hive Cell", "not visible");
        } else {
            telemetry.addData("Upward Hive Cell", target.cell);
        }
        telemetry.addLine("Launcher remains stopped until PLAY");
        telemetry.update();
    }

    @Override
    public void start() {
        matchTimer.reset();
        transition(State.DRIVE_TO_SHOT);
        drivetrain.followPath(plan.toShoot);
    }

    @Override
    public void loop() {
        drivetrain.periodic();
        superstructure.periodic();

        if (matchTimer.seconds() >= RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS) {
            drivetrain.stop();
            superstructure.stopAll();
            transition(State.DONE);
        } else {
            runState();
        }
        publishTelemetry();
    }

    private void runState() {
        switch (state) {
            case DRIVE_TO_SHOT:
                if (!drivetrain.isBusy()) {
                    drivetrain.stop();
                    transition(State.ALIGN_AND_SHOOT);
                }
                break;

            case ALIGN_AND_SHOOT:
                if (superstructure.inventory.peekNext() == null
                        || matchTimer.seconds() >= RobotConfig.Auto.SHOOT_CUTOFF_SECONDS) {
                    beginPark();
                } else {
                    aimAndShootAtUpwardCell();
                }
                break;

            case DRIVE_TO_PARK:
                if (!drivetrain.isBusy()) {
                    drivetrain.holdCurrentPose();
                    superstructure.shooter.stop();
                    transition(State.DONE);
                }
                break;

            case DONE:
                break;
        }
    }

    private void beginPark() {
        drivetrain.stop();
        superstructure.shooter.stop();
        transition(State.DRIVE_TO_PARK);
        drivetrain.followPath(plan.toPark);
    }

    /**
     * Uses the tag cluster under the upward-facing Cell to prevent shooting at the closed side.
     * The same Cell must remain visible and centered briefly before every feed pulse, so a moving
     * Hive naturally pauses shooting until it settles into its next stable position.
     */
    private void aimAndShootAtUpwardCell() {
        superstructure.prepareHiveShot();
        if (superstructure.isBusy()) {
            // Do not turn during a feed pulse, and start the next stability check after it finishes.
            drivetrain.stop();
            alignedCellTimer.reset();
            return;
        }

        BiobuzzVision.HiveTarget target = superstructure.vision.upwardHiveTarget(alliance);
        if (target == null) {
            drivetrain.stop();
            clearAlignedCell();
            return;
        }

        double bearingError = target.bearingDegrees
                - RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES;
        if (Math.abs(bearingError) > RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES) {
            double turnPower = bearingError
                    * RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE;
            turnPower = Math.max(-RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER,
                    Math.min(RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER, turnPower));
            drivetrain.turnInPlace(turnPower);
            clearAlignedCell();
            return;
        }

        drivetrain.stop();
        if (target.cell != lastAlignedCell) {
            lastAlignedCell = target.cell;
            alignedCellTimer.reset();
        }
        if (alignedCellTimer.seconds() < RobotConfig.Vision.HIVE_AIM_HOLD_SECONDS) {
            return;
        }
        if (superstructure.queueHiveShot()) {
            shotsRequested++;
            // Require a fresh stable-target interval before feeding the next pollen.
            alignedCellTimer.reset();
        }
    }

    private void clearAlignedCell() {
        lastAlignedCell = null;
        alignedCellTimer.reset();
    }

    private void transition(State next) {
        state = next;
    }

    private void publishTelemetry() {
        telemetry.addData("Alliance", alliance);
        telemetry.addData("State", state);
        telemetry.addData("Auto elapsed", "%.1f", matchTimer.seconds());
        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.addData("Inventory", superstructure.inventory.snapshot());
        telemetry.addData("Shots requested", shotsRequested);
        telemetry.addData("Limelight connected", superstructure.vision.isConnected());
        BiobuzzVision.HiveTarget target = superstructure.vision.upwardHiveTarget(alliance);
        if (target == null) {
            telemetry.addData("Hive target", "not visible");
        } else {
            telemetry.addData("Hive target", "%s | bearing: %.1f | tags: %d",
                    target.cell, target.bearingDegrees, target.visibleTagCount);
        }
        telemetry.update();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        superstructure.close();
    }
}

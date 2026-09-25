package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.auto.BiobuzzAutoPlan;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.BiobuzzVision;
import org.firstinspires.ftc.teamcode.vision.HiveAim;

/** Shared, timeout-protected preload scoring autonomous. */
abstract class BiobuzzAutoBase extends OpMode {
    private enum State { DRIVE_TO_SHOT, ALIGN_AND_SHOOT, DRIVE_TO_PARK, DONE }

    private final AllianceColor alliance;
    private final ElapsedTime matchTimer = new ElapsedTime();
    private final HiveAim hiveAim = new HiveAim();
    private final ElapsedTime sinceFeed = new ElapsedTime();
    private Drivetrain drivetrain;
    private Superstructure superstructure;
    private BiobuzzAutoPlan plan;
    private State state;
    private int shotsRequested;
    private BiobuzzVision.HiveTarget hiveTarget;

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
        // Tag orientation and the opening geometry qualify a target; visibility alone does not.
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
            telemetry.addData("Hive opening estimate", "no usable fresh upright target");
        } else {
            telemetry.addData("Hive opening estimate", target.cell);
        }
        telemetry.addLine("Camera: upright mount, 82.55-mm tags, Full 3D required");
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
        if (matchTimer.seconds() >= RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS) {
            drivetrain.stop();
            superstructure.stopAll();
            transition(State.DONE);
        } else {
            drivetrain.periodic();
            boolean wasFeeding = superstructure.isBusy();
            superstructure.periodic();
            if (wasFeeding && !superstructure.isBusy()) {
                sinceFeed.reset();
            }
            hiveTarget = superstructure.vision.upwardHiveTarget(alliance);
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
                    // Normal transitions finish a feed; the separate 29-second fail-safe is immediate.
                    if (!superstructure.isBusy()) {
                        beginPark();
                    }
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
        hiveAim.reset();
        drivetrain.stop();
        superstructure.shooter.stop();
        transition(State.DRIVE_TO_PARK);
        drivetrain.followPath(plan.toPark);
    }

    /**
     * Turns toward the estimated opening from our fixed shot position. Feeding also requires fresh
     * 3D poses with little motion and a ready flywheel. The post-feed wait allows flight/tip onset;
     * all thresholds need field validation and cannot guarantee a mechanical damper has settled.
     */
    private void aimAndShootAtUpwardCell() {
        superstructure.prepareHiveShot();
        if (superstructure.isBusy()) {
            // Do not turn during a feed pulse, and start the next stability check after it finishes.
            drivetrain.stop();
            hiveAim.reset();
            return;
        }

        BiobuzzVision.HiveTarget target = hiveTarget;
        if (target == null) {
            drivetrain.stop();
            hiveAim.reset();
            return;
        }

        double bearingError = target.bearingDegrees
                - RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES;
        if (Math.abs(bearingError) > RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES) {
            drivetrain.turnInPlace(HiveAim.turnPower(target.bearingDegrees));
            hiveAim.reset();
            return;
        }

        drivetrain.stop();
        if (shotsRequested > 0
                && sinceFeed.seconds() < RobotConfig.Vision.HIVE_POST_FEED_WAIT_SECONDS) {
            hiveAim.reset();
            return;
        }
        // Do not start a pulse that cannot finish before the user-selected shooting cutoff.
        if (matchTimer.seconds() + RobotConfig.Shooter.FEED_SECONDS
                >= RobotConfig.Auto.SHOOT_CUTOFF_SECONDS) {
            return;
        }
        if (hiveAim.readyToFeed(target, matchTimer.seconds())
                && superstructure.queueHiveShot(true)) {
            shotsRequested++;
            hiveAim.reset();
            sinceFeed.reset();
        }
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
        BiobuzzVision.HiveTarget target = hiveTarget;
        if (target == null) {
            telemetry.addData("Hive target", "no usable fresh upright target");
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

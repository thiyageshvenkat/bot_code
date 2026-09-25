package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.auto.BiobuzzAutoPlan;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.BiobuzzVision;

/**
 * Shared implementation behind the registered red and blue BIOBUZZ autonomous OpModes.
 *
 * <p>This is deliberately a conservative preload routine: follow the alliance plan to one fixed
 * shooting position, qualify a visible Hive opening, feed the four assumed preloads, and drive to
 * the loading-zone park. It does not yet execute the Garden/Flower portions retained in
 * BiobuzzAutoPlan or reposition around the Hive after a tip. The concrete red/blue classes exist so
 * the Driver Station can register two choices while this lifecycle and safety logic stays shared.</p>
 */
abstract class BiobuzzAutoBase extends OpMode {
    // State boundaries prevent path following, vision turning, and parking from commanding the
    // drivetrain at the same time. DONE remains active until FTC calls stop().
    private enum State { DRIVE_TO_SHOT, ALIGN_AND_SHOOT, DRIVE_TO_PARK, DONE }

    // Alliance affects both route mirroring and which Hive tag IDs are acceptable.
    private final AllianceColor alliance;
    // One match clock makes cutoff decisions independent of how long any path or shot takes.
    private final ElapsedTime matchTimer = new ElapsedTime();
    // Starts when a feed pulse actually finishes, so the delay includes ball flight/tip onset rather
    // than time spent pushing the pollen through the feeder.
    private final ElapsedTime sinceFeed = new ElapsedTime();
    private Drivetrain drivetrain;
    private Superstructure superstructure;
    private BiobuzzAutoPlan plan;
    private State state;
    // Counts accepted feed requests for driver-station diagnostics, not confirmed scored pollen.
    private int shotsRequested;
    // Read once per loop and shared by control and telemetry so they describe the same camera frame.
    private BiobuzzVision.HiveTarget hiveTarget;

    /** Receives the fixed alliance from BiobuzzRedAuto or BiobuzzBlueAuto. */
    BiobuzzAutoBase(AllianceColor alliance) {
        this.alliance = alliance;
    }

    @Override
    public void init() {
        // INIT establishes localization and the four-preload software assumption. Shooter
        // construction also stows the hood, so physical clearance must be checked before INIT.
        plan = BiobuzzAutoPlan.forAlliance(alliance);
        drivetrain = new Drivetrain(hardwareMap);
        drivetrain.startAuto();
        drivetrain.setPose(plan.start);
        superstructure = new Superstructure(hardwareMap, alliance);
        superstructure.seedPreloadPollen();
        // The required Hive camera starts its fixed AprilTag pipeline in BiobuzzVision's
        // constructor. Tag orientation and opening geometry qualify a target; visibility alone does
        // not. The optional pollen camera has no effect on this autonomous routine.
        telemetry.addData("BIOBUZZ auto", alliance);
        telemetry.addLine("Check start pose, visible own-Hive tags, and clear park route");
    }

    @Override
    public void init_loop() {
        // This is observation-only: the launcher and drivetrain stay stopped while the drive team
        // checks the chosen alliance, taped start pose, camera mount, and target visibility.
        telemetry.addData("Start pose", plan.start);
        telemetry.addData("Hive Limelight connected",
                superstructure.vision.isHiveCameraConnected());
        BiobuzzVision.HiveTarget target =
                superstructure.vision.findUpwardCellOpening(alliance);
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
        // FTC calls start at the beginning of the 30-second period. All timing must begin here,
        // rather than during an unpredictably long INIT wait.
        matchTimer.reset();
        transition(State.DRIVE_TO_SHOT);
        drivetrain.followPath(plan.toShoot);
    }

    @Override
    public void loop() {
        // The hard cutoff takes priority over state logic. It is intentionally allowed to interrupt
        // an active feeder pulse because stopping before Auto ends is the higher-level safety rule.
        if (matchTimer.seconds() >= RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS) {
            drivetrain.stop();
            superstructure.stopAll();
            transition(State.DONE);
        } else {
            // Update hardware before making the next decision so completion, RPM, pose, and camera
            // observations describe the newest available control cycle.
            drivetrain.periodic();
            boolean wasFeeding = superstructure.isBusy();
            superstructure.periodic();
            if (wasFeeding && !superstructure.isBusy()) {
                // A completed pulse begins the deliberate delay before the next Hive qualification.
                sinceFeed.reset();
            }
            hiveTarget = superstructure.vision.findUpwardCellOpening(alliance);
            runState();
        }
        publishTelemetry();
    }

    private void runState() {
        switch (state) {
            case DRIVE_TO_SHOT:
                // Pedro owns the drivetrain until the path reports complete; vision does not fight
                // the path follower while approaching the fixed shooting position.
                if (!drivetrain.isBusy()) {
                    drivetrain.stop();
                    transition(State.ALIGN_AND_SHOOT);
                }
                break;

            case ALIGN_AND_SHOOT:
                // Auto tracks only its four assumed preloads. It parks after those pulses or when
                // the configured shooting window closes, even if a shot was not physically sensed.
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
                // Hold the final pose instead of leaving the drivetrain uncontrolled after arrival.
                if (!drivetrain.isBusy()) {
                    drivetrain.holdCurrentPose();
                    superstructure.shooter.stop();
                    transition(State.DONE);
                }
                break;

            case DONE:
                // Outputs were already made safe during the transition that reached this state.
                break;
        }
    }

    /** Ends targeting, makes the launcher safe, and hands drivetrain ownership back to Pedro. */
    private void beginPark() {
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
        // Spinning up may happen while vision is still qualifying the opening, saving Auto time.
        superstructure.prepareHiveShot();
        if (superstructure.isBusy()) {
            // Do not turn while pollen is being pushed into the flywheel.
            drivetrain.stop();
            return;
        }

        BiobuzzVision.HiveTarget target = hiveTarget;
        if (target == null) {
            // Without a fresh target, the robot does not know which way to turn or where it would
            // shoot. BiobuzzVision has already rejected old and frozen camera images.
            drivetrain.stop();
            return;
        }

        // target.bearingDegrees is where the estimated opening currently appears relative to the
        // Hive camera: positive is camera-right and negative is camera-left. The configured bearing
        // is where that opening SHOULD appear when the offset launcher is aimed correctly. Their
        // difference is therefore the remaining horizontal turn error; zero means aligned.
        double bearingError = target.bearingDegrees
                - RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES;
        if (Math.abs(bearingError) > RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES) {
            // Only rotate here; the path already placed the robot at the calibrated shot distance.
            double turnPower = -bearingError * RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE;
            turnPower = Math.max(-RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER,
                    Math.min(RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER, turnPower));
            drivetrain.turnInPlace(turnPower);
            return;
        }

        drivetrain.stop();
        if (shotsRequested > 0
                && sinceFeed.seconds() < RobotConfig.Vision.HIVE_POST_FEED_WAIT_SECONDS) {
            // This delay defaults to zero. Increase it only if robot testing shows that firing the
            // next pollen immediately causes a real problem.
            return;
        }
        // Do not start a pulse that cannot finish before the user-selected shooting cutoff.
        if (matchTimer.seconds() + RobotConfig.Shooter.FEED_SECONDS
                >= RobotConfig.Auto.SHOOT_CUTOFF_SECONDS) {
            return;
        }
        if (superstructure.queueHiveShot(true)) {
            // This records an initiated pulse. Superstructure removes the inventory entry only when
            // the timed pulse finishes; without a beam sensor neither event proves a successful shot.
            shotsRequested++;
            sinceFeed.reset();
        }
    }

    /** Keeps state assignment in one place so future transition logging can be added consistently. */
    private void transition(State next) {
        state = next;
    }

    /** Publishes control-relevant evidence so a failed run can be diagnosed from Driver Station. */
    private void publishTelemetry() {
        telemetry.addData("Alliance", alliance);
        telemetry.addData("State", state);
        telemetry.addData("Auto elapsed", "%.1f", matchTimer.seconds());
        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.addData("Inventory", superstructure.inventory.snapshot());
        telemetry.addData("Shots requested", shotsRequested);
        telemetry.addData("Hive Limelight connected",
                superstructure.vision.isHiveCameraConnected());
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
        // FTC can stop the OpMode from any state, including during a path or feed pulse.
        drivetrain.stop();
        superstructure.close();
    }
}

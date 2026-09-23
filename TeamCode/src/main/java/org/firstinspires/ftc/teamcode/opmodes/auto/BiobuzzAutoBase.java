package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.auto.BiobuzzAutoPlan;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.control.Superstructure;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.vision.HiveObservation;

/** Shared, timeout-protected preload scoring autonomous. */
abstract class BiobuzzAutoBase extends OpMode {
    private enum State { DRIVE_TO_SHOT, ALIGN_AND_SHOOT, DRIVE_TO_PARK, DONE }

    private final AllianceColor alliance;
    private final ElapsedTime matchTimer = new ElapsedTime();
    private final ElapsedTime stateTimer = new ElapsedTime();
    private Drivetrain drivetrain;
    private Superstructure superstructure;
    private BiobuzzAutoPlan plan;
    private State state;
    private int shotsRequested;

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
        telemetry.addData("BIOBUZZ auto", alliance);
        telemetry.addLine("Check start pose, hive orientation, and clear park route");
    }

    @Override
    public void init_loop() {
        HiveObservation target = superstructure.vision.closestCell();
        telemetry.addData("Start pose", plan.start);
        telemetry.addData("Hive target", target == null ? "not visible"
                : String.format("%.1f in, %.1f deg", target.rangeInches, target.bearingDegrees));
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
                if (!drivetrain.isBusy()
                        || stateTimer.seconds() >= RobotConfig.Auto.PATH_TIMEOUT_SECONDS) {
                    drivetrain.stop();
                    transition(State.ALIGN_AND_SHOOT);
                }
                break;

            case ALIGN_AND_SHOOT:
                superstructure.prepareHiveShot();
                drivetrain.setRawMovement(0, 0, superstructure.vision.aimTurnPower());
                if (superstructure.inventory.peekNext() == null
                        || matchTimer.seconds() >= RobotConfig.Auto.SHOOT_CUTOFF_SECONDS) {
                    beginPark();
                } else if (!superstructure.isBusy()
                        && superstructure.queueHiveShot(true)) {
                    shotsRequested++;
                    stateTimer.reset();
                } else if (shotsRequested == 0
                        && stateTimer.seconds() >= RobotConfig.Auto.ALIGN_TIMEOUT_SECONDS) {
                    beginPark();
                }
                break;

            case DRIVE_TO_PARK:
                if (!drivetrain.isBusy()
                        || stateTimer.seconds() >= RobotConfig.Auto.PATH_TIMEOUT_SECONDS) {
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

    private void transition(State next) {
        state = next;
        stateTimer.reset();
    }

    private void publishTelemetry() {
        HiveObservation target = superstructure.vision.closestCell();
        telemetry.addData("Alliance", alliance);
        telemetry.addData("State", state);
        telemetry.addData("Auto elapsed", "%.1f", matchTimer.seconds());
        telemetry.addData("Pose", drivetrain.getPose());
        telemetry.addData("Inventory", superstructure.inventory.snapshot());
        telemetry.addData("Shots requested", shotsRequested);
        telemetry.addData("Hive", target == null ? "not visible"
                : String.format("%.1f in, %.1f deg", target.rangeInches, target.bearingDegrees));
        telemetry.update();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        superstructure.close();
    }
}

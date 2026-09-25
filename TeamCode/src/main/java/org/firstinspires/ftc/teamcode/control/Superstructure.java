package org.firstinspires.ftc.teamcode.control;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.game.ElementInventory;
import org.firstinspires.ftc.teamcode.game.ScoringElement;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.vision.BiobuzzVision;

/**
 * Coordination layer between an OpMode and the robot mechanisms other than the drivetrain.
 * Intake and Shooter own their individual hardware behavior; this class owns decisions that span
 * mechanisms, such as allowing a feed only when both inventory and shooter state permit it.
 */
public final class Superstructure implements AutoCloseable {
    // Feeding and the optional intake-capacity lockout rely on this record. Until sensors update it,
    // it is only an assumption and must not be treated as proof of what is physically in the robot.
    public final ElementInventory inventory;
    public final Intake intake;
    public final Shooter shooter;
    public final BiobuzzVision vision;

    /**
     * Creates the mechanisms that competition TeleOp and autonomous use together. Consequently,
     * every named device for intake, shooter, hood, feeder, and Limelight must exist in the active
     * REV configuration even if a particular test intends to operate only one of them.
     */
    public Superstructure(HardwareMap hardwareMap, AllianceColor alliance) {
        inventory = new ElementInventory(alliance);
        intake = new Intake(hardwareMap);
        shooter = new Shooter(hardwareMap);
        vision = new BiobuzzVision(hardwareMap);
    }

    /**
     * Applies the competition-start assumption that all four allowed positions contain pollen.
     * This lets shooting work before element sensors exist, but it is deliberately not called by
     * Pit Diagnostics because an empty test robot would then have an incorrect inventory.
     */
    public void seedPreloadPollen() {
        inventory.clear();
        for (int i = 0; i < ElementInventory.CAPACITY; i++) {
            inventory.tryAdd(ScoringElement.POLLEN);
        }
    }

    /**
     * Uses the near-distance calibration as the standard hive shot. Keeping this choice here keeps
     * TeleOp and autonomous from independently choosing different RPM and hood targets.
     */
    public void prepareHiveShot() {
        shooter.prepare(ShotModel.forDistance(RobotConfig.Shooter.NEAR_DISTANCE_IN));
    }

    /**
     * Requests one feeder cycle only when the software inventory is nonempty and the shooter is
     * ready. Checking both avoids deliberately dry-feeding and avoids pushing an element into a
     * flywheel that has not reached speed.
     */
    public boolean queueHiveShot() {
        return inventory.peekNext() != null && shooter.requestFeed();
    }

    /**
     * Must run once per OpMode loop so the timed feeder state can advance. Inventory is decremented
     * after the feed pulse completes—not when requested—so a rejected request cannot lose an entry.
     * Without a feeder sensor, this remains an assumption rather than proof that pollen was fired.
     */
    public void periodic() {
        shooter.periodic();
        if (shooter.consumeFeedPulseCompleted()) {
            inventory.releaseNext();
        }
    }

    /** Allows autonomous state transitions to wait until the active feed cycle has finished. */
    public boolean isBusy() {
        return shooter.getState() == Shooter.State.FEEDING;
    }

    /** Common safety stop used when an OpMode finishes or autonomous reaches its time limit. */
    public void stopAll() {
        intake.stop();
        shooter.stop();
    }

    /** Also stops Limelight polling so the camera is not left active after the OpMode ends. */
    @Override
    public void close() {
        stopAll();
        vision.close();
    }
}

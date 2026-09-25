package org.firstinspires.ftc.teamcode.control;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.game.ElementInventory;
import org.firstinspires.ftc.teamcode.game.ScoringElement;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.vision.PollenVision;

/** Coordinates the confirmed intake, launcher, and Limelight hardware. */
public final class Superstructure implements AutoCloseable {
    // Software record of the elements believed to be inside the robot; this is not a sensor.
    public final ElementInventory inventory;
    // Physical mechanism that collects or ejects elements.
    public final Intake intake;
    // Physical flywheel, feeder, and hood used to score elements.
    public final Shooter shooter;
    // Limelight wrapper used to find pollen targets.
    public final PollenVision vision;

    /** Creates each non-drivetrain subsystem and an empty inventory for the selected alliance. */
    public Superstructure(HardwareMap hardwareMap, AllianceColor alliance) {
        inventory = new ElementInventory(alliance);
        intake = new Intake(hardwareMap);
        shooter = new Shooter(hardwareMap);
        vision = new PollenVision(hardwareMap);
    }

    /**
     * Resets the software inventory to four starting pollen elements.
     * This changes only the inventory model; it does not run or inspect any physical mechanism.
     * Do not call this for an empty-robot test.
     */
    public void seedPreloadPollen() {
        inventory.clear();
        for (int i = 0; i < ElementInventory.CAPACITY; i++) {
            inventory.tryAdd(ScoringElement.POLLEN);
        }
    }

    /** Spins the flywheel toward the configured near-distance shot and positions the hood. */
    public void prepareHiveShot() {
        shooter.prepare(ShotModel.forDistance(RobotConfig.Shooter.NEAR_DISTANCE_IN));
    }

    /**
     * Requests one feeder cycle only when the software inventory is nonempty and the shooter is
     * ready. The inventory entry is not removed until the shooter reports that feeding completed.
     */
    public boolean queueHiveShot() {
        return inventory.peekNext() != null && shooter.requestFeed();
    }

    /** Advances shooter timing and keeps software inventory synchronized with completed shots. */
    public void periodic() {
        shooter.periodic();
        if (shooter.consumeShotCompleted()) {
            inventory.releaseNext();
        }
    }

    /** Returns whether the feeder is currently moving an element into the flywheel. */
    public boolean isBusy() {
        return shooter.getState() == Shooter.State.FEEDING;
    }

    /** Immediately stops the intake, flywheel, and feeder. */
    public void stopAll() {
        intake.stop();
        shooter.stop();
    }

    /** Stops every mechanism and releases the Limelight when the OpMode ends. */
    @Override
    public void close() {
        stopAll();
        vision.close();
    }
}

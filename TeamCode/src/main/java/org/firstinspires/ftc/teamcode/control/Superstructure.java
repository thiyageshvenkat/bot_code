package org.firstinspires.ftc.teamcode.control;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.ShooterConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.game.ElementInventory;
import org.firstinspires.ftc.teamcode.game.ScoringElement;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.vision.PollenVision;

/** Coordinates the confirmed intake, launcher, and Limelight hardware. */
public final class Superstructure implements AutoCloseable {
    public final ElementInventory inventory;
    public final Intake intake;
    public final Shooter shooter;
    public final PollenVision vision;

    public Superstructure(HardwareMap hardwareMap, AllianceColor alliance) {
        inventory = new ElementInventory(alliance);
        intake = new Intake(hardwareMap);
        shooter = new Shooter(hardwareMap);
        vision = new PollenVision(hardwareMap);
    }

    public void seedPreloadPollen() {
        inventory.clear();
        for (int i = 0; i < 4; i++) inventory.tryAdd(ScoringElement.POLLEN);
    }

    public void prepareHiveShot() {
        shooter.prepare(ShotModel.forDistance(ShooterConfig.NEAR_DISTANCE_IN));
    }

    public boolean queueHiveShot() {
        return inventory.peekNext() != null && shooter.requestFeed();
    }

    public void periodic() {
        shooter.periodic();
        if (shooter.consumeShotCompleted()) inventory.releaseNext();
    }

    public boolean isBusy() {
        return shooter.getState() == Shooter.State.FEEDING;
    }

    public void stopAll() {
        intake.stop();
        shooter.stop();
    }

    @Override
    public void close() {
        stopAll();
        vision.close();
    }
}

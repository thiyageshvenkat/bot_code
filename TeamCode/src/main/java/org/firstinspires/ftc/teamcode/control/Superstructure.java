package org.firstinspires.ftc.teamcode.control;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.firstinspires.ftc.teamcode.game.BiobuzzRules;
import org.firstinspires.ftc.teamcode.game.ElementInventory;
import org.firstinspires.ftc.teamcode.game.ScoringElement;
import org.firstinspires.ftc.teamcode.constants.ShooterConfig;
import org.firstinspires.ftc.teamcode.subsystems.FlowerArm;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Magazine;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.vision.HiveObservation;
import org.firstinspires.ftc.teamcode.vision.HiveVision;

/** Owns cross-mechanism sequencing and prevents rule-unsafe scoring actions. */
public final class Superstructure implements AutoCloseable {
    private enum Route { NONE, HIVE, FLOWER }

    public final ElementInventory inventory;
    public final Intake intake;
    public final Magazine magazine;
    public final Shooter shooter;
    public final FlowerArm flower;
    public final HiveVision vision;

    private Route route = Route.NONE;
    private boolean magazineRequested;
    private boolean stagedForShooter;
    private boolean flowerLoaded;

    public Superstructure(HardwareMap hardwareMap, AllianceColor alliance) {
        inventory = new ElementInventory(alliance);
        intake = new Intake(hardwareMap, inventory);
        magazine = new Magazine(hardwareMap, inventory);
        shooter = new Shooter(hardwareMap);
        flower = new FlowerArm(hardwareMap);
        vision = new HiveVision(hardwareMap);
    }

    public void seedPreloadPollen() {
        inventory.clear();
        for (int i = 0; i < 4; i++) inventory.tryAdd(ScoringElement.POLLEN);
    }

    public void prepareHiveShot() {
        HiveObservation target = vision.closestCell();
        double distance = target == null
                ? ShooterConfig.NEAR_DISTANCE_IN
                : target.rangeInches;
        shooter.prepare(ShotModel.forDistance(distance));
    }

    public boolean queueHiveShot(boolean requireVisionAlignment) {
        if (route != Route.NONE || inventory.peekNext() == null || !shooter.isReady()) return false;
        if (requireVisionAlignment && !vision.isAligned()) return false;
        route = Route.HIVE;
        magazineRequested = magazine.requestFeed();
        if (!magazineRequested) route = Route.NONE;
        return magazineRequested;
    }

    public boolean queueFlowerTransfer(double teleopElapsedSeconds) {
        ScoringElement next = inventory.peekNext();
        if (route != Route.NONE || next == null
                || !BiobuzzRules.canPlaceInFlower(next, teleopElapsedSeconds)) return false;
        route = Route.FLOWER;
        flowerLoaded = false;
        magazineRequested = false;
        flower.raiseToFlower();
        return true;
    }

    public boolean depositFlower(double teleopElapsedSeconds) {
        ScoringElement next = inventory.peekNext();
        if (route != Route.FLOWER || !flowerLoaded || next == null
                || !BiobuzzRules.canPlaceInFlower(next, teleopElapsedSeconds)) return false;
        if (!flower.deposit()) return false;
        inventory.releaseNext();
        route = Route.NONE;
        flowerLoaded = false;
        return true;
    }

    public void periodic() {
        intake.periodic();
        magazine.periodic();
        shooter.periodic();
        flower.periodic();

        if (route == Route.FLOWER && flower.atScoreHeight() && !magazineRequested && !flowerLoaded) {
            magazineRequested = magazine.requestFeed();
        }

        if (magazine.consumeFeedCompleted()) {
            if (route == Route.HIVE) stagedForShooter = true;
            else if (route == Route.FLOWER) flowerLoaded = true;
        }

        if (route == Route.HIVE && stagedForShooter && shooter.isReady()
                && shooter.requestFeed()) {
            stagedForShooter = false;
        }

        if (shooter.consumeShotCompleted()) {
            inventory.releaseNext();
            route = Route.NONE;
            magazineRequested = false;
        }
    }

    public boolean isBusy() {
        return route != Route.NONE || magazine.isFeeding()
                || shooter.getState() == Shooter.State.FEEDING;
    }

    public String getRoute() { return route.name(); }
    public boolean isFlowerLoaded() { return flowerLoaded; }

    public void stopAll() {
        intake.stop();
        magazine.cancel();
        shooter.stop();
        flower.emergencyStop();
        route = Route.NONE;
    }

    @Override
    public void close() {
        stopAll();
        vision.close();
    }
}

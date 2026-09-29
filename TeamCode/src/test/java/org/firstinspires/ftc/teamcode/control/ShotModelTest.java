package org.firstinspires.ftc.teamcode.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class ShotModelTest {
    private double oldBaseRpm;
    private double oldNearScale;
    private double oldFarScale;
    private double oldNearHood;
    private double oldFarHood;
    private double oldNearDistance;
    private double oldFarDistance;

    @Before
    public void useSimpleCalibration() {
        oldBaseRpm = RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM;
        oldNearScale = RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE;
        oldFarScale = RobotConfig.Shooter.FAR_SHOT_RPM_SCALE;
        oldNearHood = RobotConfig.Shooter.HOOD_NEAR;
        oldFarHood = RobotConfig.Shooter.HOOD_FAR;
        oldNearDistance = RobotConfig.Shooter.NEAR_DISTANCE_IN;
        oldFarDistance = RobotConfig.Shooter.FAR_DISTANCE_IN;

        RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM = 4000;
        RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE = .8;
        RobotConfig.Shooter.FAR_SHOT_RPM_SCALE = 1.2;
        RobotConfig.Shooter.HOOD_NEAR = .3;
        RobotConfig.Shooter.HOOD_FAR = .7;
        RobotConfig.Shooter.NEAR_DISTANCE_IN = 20;
        RobotConfig.Shooter.FAR_DISTANCE_IN = 100;
    }

    @After
    public void restoreCalibration() {
        RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM = oldBaseRpm;
        RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE = oldNearScale;
        RobotConfig.Shooter.FAR_SHOT_RPM_SCALE = oldFarScale;
        RobotConfig.Shooter.HOOD_NEAR = oldNearHood;
        RobotConfig.Shooter.HOOD_FAR = oldFarHood;
        RobotConfig.Shooter.NEAR_DISTANCE_IN = oldNearDistance;
        RobotConfig.Shooter.FAR_DISTANCE_IN = oldFarDistance;
    }

    @Test public void returnsExactCalibrationEndpointsAndMidpoint() {
        assertShot(ShotModel.forDistance(20), 3200, .3);
        assertShot(ShotModel.forDistance(60), 4000, .5);
        assertShot(ShotModel.forDistance(100), 4800, .7);
    }

    @Test public void tableClampsOutsideCalibratedRange() {
        assertShot(ShotModel.forDistance(-Double.MAX_VALUE), 3200, .3);
        assertShot(ShotModel.forDistance(0), 3200, .3);
        assertShot(ShotModel.forDistance(Double.MAX_VALUE), 4800, .7);
    }

    @Test public void invalidDistanceFallsBackToNearCalibration() {
        for (double invalid : new double[] {
                Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
        }) {
            assertShot(ShotModel.forDistance(invalid), 3200, .3);
        }
    }

    @Test public void equalCalibrationDistancesDoNotDivideByZero() {
        RobotConfig.Shooter.FAR_DISTANCE_IN = RobotConfig.Shooter.NEAR_DISTANCE_IN;
        for (double distance : new double[] {-100, 20, 50, 1000}) {
            assertShot(ShotModel.forDistance(distance), 3200, .3);
        }
    }

    @Test public void reversedCalibrationDistancesStillUseTheirNamedEndpoints() {
        RobotConfig.Shooter.NEAR_DISTANCE_IN = 100;
        RobotConfig.Shooter.FAR_DISTANCE_IN = 20;
        assertShot(ShotModel.forDistance(100), 3200, .3);
        assertShot(ShotModel.forDistance(60), 4000, .5);
        assertShot(ShotModel.forDistance(20), 4800, .7);
        assertShot(ShotModel.forDistance(1000), 3200, .3);
        assertShot(ShotModel.forDistance(-1000), 4800, .7);
    }

    @Test public void decreasingRpmAndHoodCalibrationAlsoInterpolatesCorrectly() {
        RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE = 1.2;
        RobotConfig.Shooter.FAR_SHOT_RPM_SCALE = .8;
        RobotConfig.Shooter.HOOD_NEAR = .7;
        RobotConfig.Shooter.HOOD_FAR = .3;
        assertShot(ShotModel.forDistance(20), 4800, .7);
        assertShot(ShotModel.forDistance(60), 4000, .5);
        assertShot(ShotModel.forDistance(100), 3200, .3);
    }

    @Test public void tenThousandRandomDistancesRemainFiniteAndWithinEndpoints() {
        Random random = new Random(23229);
        for (int i = 0; i < 10_000; i++) {
            double distance = -10_000 + random.nextDouble() * 20_000;
            ShotModel shot = ShotModel.forDistance(distance);
            assertTrue(Double.isFinite(shot.shooterTargetRpm));
            assertTrue(Double.isFinite(shot.hoodPosition));
            assertTrue(shot.shooterTargetRpm >= 3200);
            assertTrue(shot.shooterTargetRpm <= 4800);
            assertTrue(shot.hoodPosition >= .3);
            assertTrue(shot.hoodPosition <= .7);
        }
    }

    private static void assertShot(ShotModel shot, double rpm, double hood) {
        assertEquals(rpm, shot.shooterTargetRpm, 1e-9);
        assertEquals(hood, shot.hoodPosition, 1e-9);
    }
}

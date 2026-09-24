package org.firstinspires.ftc.teamcode.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.constants.ShooterConfig;
import org.junit.Test;

public class ShotModelTest {
    @Test public void distanceTableInterpolatesMonotonically() {
        ShotModel.Solution near = ShotModel.forDistance(ShooterConfig.NEAR_DISTANCE_IN);
        ShotModel.Solution middle = ShotModel.forDistance(
                (ShooterConfig.NEAR_DISTANCE_IN + ShooterConfig.FAR_DISTANCE_IN) / 2.0);
        ShotModel.Solution far = ShotModel.forDistance(ShooterConfig.FAR_DISTANCE_IN);
        assertTrue(near.flywheelVelocity < middle.flywheelVelocity);
        assertTrue(middle.flywheelVelocity < far.flywheelVelocity);
        assertTrue(near.hoodPosition < far.hoodPosition);
    }

    @Test public void tableClampsOutsideCalibratedRange() {
        assertEquals(ShotModel.forDistance(-100).hoodPosition,
                ShotModel.forDistance(ShooterConfig.NEAR_DISTANCE_IN).hoodPosition, 1e-9);
        assertEquals(ShotModel.forDistance(1000).hoodPosition,
                ShotModel.forDistance(ShooterConfig.FAR_DISTANCE_IN).hoodPosition, 1e-9);
    }
}

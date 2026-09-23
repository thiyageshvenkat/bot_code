package org.firstinspires.ftc.teamcode.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.junit.Test;

public class ShotModelTest {
    @Test public void distanceTableInterpolatesMonotonically() {
        ShotSolution near = ShotModel.forDistance(RobotConfig.Shooter.NEAR_DISTANCE_IN);
        ShotSolution middle = ShotModel.forDistance(
                (RobotConfig.Shooter.NEAR_DISTANCE_IN + RobotConfig.Shooter.FAR_DISTANCE_IN) / 2.0);
        ShotSolution far = ShotModel.forDistance(RobotConfig.Shooter.FAR_DISTANCE_IN);
        assertTrue(near.flywheelVelocity < middle.flywheelVelocity);
        assertTrue(middle.flywheelVelocity < far.flywheelVelocity);
        assertTrue(near.hoodPosition < far.hoodPosition);
    }

    @Test public void tableClampsOutsideCalibratedRange() {
        assertEquals(ShotModel.forDistance(-100).hoodPosition,
                ShotModel.forDistance(RobotConfig.Shooter.NEAR_DISTANCE_IN).hoodPosition, 1e-9);
        assertEquals(ShotModel.forDistance(1000).hoodPosition,
                ShotModel.forDistance(RobotConfig.Shooter.FAR_DISTANCE_IN).hoodPosition, 1e-9);
    }
}

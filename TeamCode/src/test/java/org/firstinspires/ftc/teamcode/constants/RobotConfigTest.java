package org.firstinspires.ftc.teamcode.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RobotConfigTest {
    @Test
    public void panelsValuesAreKeptWithinOperationalRanges() {
        double oldDeadband = RobotConfig.Drive.STICK_DEADBAND;
        double oldFeedPower = RobotConfig.Shooter.FEED_POWER;
        double oldShooterRpm = RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM;
        double oldFrameAge = RobotConfig.Vision.MAX_FRAME_AGE_MS;
        double oldMatchCutoff = RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS;
        double oldShootCutoff = RobotConfig.Auto.SHOOT_CUTOFF_SECONDS;
        try {
            RobotConfig.Drive.STICK_DEADBAND = 1;
            RobotConfig.Shooter.FEED_POWER = 4;
            RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM = 100_000;
            RobotConfig.Vision.MAX_FRAME_AGE_MS = -1;
            RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS = 40;
            RobotConfig.Auto.SHOOT_CUTOFF_SECONDS = 35;

            RobotConfig.keepLiveTuningValuesWithinSafeRanges();

            assertEquals(.95, RobotConfig.Drive.STICK_DEADBAND, 0);
            assertEquals(1, RobotConfig.Shooter.FEED_POWER, 0);
            assertEquals(RobotConfig.Shooter.SHOOTER_MOTOR_MAX_RPM,
                    RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM, 0);
            assertEquals(1, RobotConfig.Vision.MAX_FRAME_AGE_MS, 0);
            assertEquals(30, RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS, 0);
            assertEquals(30, RobotConfig.Auto.SHOOT_CUTOFF_SECONDS, 0);
        } finally {
            RobotConfig.Drive.STICK_DEADBAND = oldDeadband;
            RobotConfig.Shooter.FEED_POWER = oldFeedPower;
            RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM = oldShooterRpm;
            RobotConfig.Vision.MAX_FRAME_AGE_MS = oldFrameAge;
            RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS = oldMatchCutoff;
            RobotConfig.Auto.SHOOT_CUTOFF_SECONDS = oldShootCutoff;
        }
    }
}

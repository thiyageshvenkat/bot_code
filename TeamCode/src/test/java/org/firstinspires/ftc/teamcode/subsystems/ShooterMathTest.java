package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class ShooterMathTest {
    private double oldTicksPerRevolution;

    @Before
    public void useKnownEncoder() {
        oldTicksPerRevolution =
                RobotConfig.Shooter.SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION;
        RobotConfig.Shooter.SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION = 28;
    }

    @After
    public void restoreEncoder() {
        RobotConfig.Shooter.SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION =
                oldTicksPerRevolution;
    }

    @Test public void knownRpmValuesConvertToExpectedEncoderVelocity() {
        assertEquals(0, Shooter.rpmToTicksPerSecond(0), 0);
        assertEquals(28, Shooter.rpmToTicksPerSecond(60), 1e-12);
        assertEquals(2800, Shooter.rpmToTicksPerSecond(6000), 1e-12);
        assertEquals(6000, Shooter.ticksPerSecondToRpm(2800), 1e-12);
    }

    @Test public void conversionRoundTripsAcrossTenThousandSpeeds() {
        Random random = new Random(23229);
        for (int i = 0; i < 10_000; i++) {
            double rpm = random.nextDouble() * RobotConfig.Shooter.SHOOTER_MOTOR_MAX_RPM;
            assertEquals(rpm,
                    Shooter.ticksPerSecondToRpm(Shooter.rpmToTicksPerSecond(rpm)), 1e-9);
        }
    }

    @Test public void conversionUsesConfiguredMotorEncoderResolution() {
        RobotConfig.Shooter.SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION = 145.1;
        assertEquals(145.1, Shooter.rpmToTicksPerSecond(60), 1e-12);
        assertEquals(60, Shooter.ticksPerSecondToRpm(145.1), 1e-12);
    }
}

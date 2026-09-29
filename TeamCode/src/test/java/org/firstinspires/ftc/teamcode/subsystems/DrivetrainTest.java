package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class DrivetrainTest {
    private double oldDeadband;

    @Before
    public void useKnownDeadband() {
        oldDeadband = RobotConfig.Drive.STICK_DEADBAND;
        RobotConfig.Drive.STICK_DEADBAND = .1;
    }

    @After
    public void restoreDeadband() {
        RobotConfig.Drive.STICK_DEADBAND = oldDeadband;
    }

    @Test public void deadbandAndEndpointsAreExact() {
        assertEquals(0, Drivetrain.shape(0), 0);
        assertEquals(0, Drivetrain.shape(.1), 0);
        assertEquals(0, Drivetrain.shape(-.1), 0);
        assertEquals(1, Drivetrain.shape(1), 0);
        assertEquals(-1, Drivetrain.shape(-1), 0);
    }

    @Test public void inputIsClippedBeforeShaping() {
        assertEquals(1, Drivetrain.shape(Double.MAX_VALUE), 0);
        assertEquals(-1, Drivetrain.shape(-Double.MAX_VALUE), 0);
    }

    @Test public void midpointUsesCubicFineControl() {
        // Halfway through the usable stick range becomes 0.5 cubed.
        assertEquals(.125, Drivetrain.shape(.55), 1e-12);
        assertEquals(-.125, Drivetrain.shape(-.55), 1e-12);
    }

    @Test public void invalidInputCannotBecomeInvalidMotorMath() {
        assertEquals(0, Drivetrain.shape(Double.NaN), 0);
        assertEquals(0, Drivetrain.shape(Double.POSITIVE_INFINITY), 0);
        assertEquals(0, Drivetrain.shape(Double.NEGATIVE_INFINITY), 0);
    }

    @Test public void randomOutputsStayBoundedOddAndMonotonic() {
        Random random = new Random(23229);
        double previous = Drivetrain.shape(-1);
        for (int i = 1; i <= 10_000; i++) {
            double orderedInput = -1 + i * 2.0 / 10_000;
            double orderedOutput = Drivetrain.shape(orderedInput);
            assertTrue(orderedOutput >= previous);
            assertTrue(orderedOutput >= -1 && orderedOutput <= 1);
            previous = orderedOutput;

            double randomInput = -1000 + random.nextDouble() * 2000;
            assertEquals(-Drivetrain.shape(randomInput),
                    Drivetrain.shape(-randomInput), 1e-12);
        }
    }
}

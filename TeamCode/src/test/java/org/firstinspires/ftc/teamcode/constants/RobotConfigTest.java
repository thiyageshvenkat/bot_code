package org.firstinspires.ftc.teamcode.constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RobotConfigTest {
    private ConfigSnapshot original;

    @Before
    public void saveConfiguration() {
        original = new ConfigSnapshot();
    }

    @After
    public void restoreConfiguration() {
        original.restore();
    }

    @Test
    public void clampsEveryBoundedPanelsValueAtBothExtremes() {
        setEveryNumericValue(Double.NEGATIVE_INFINITY);
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        assertAllValuesAreSafe();

        setEveryNumericValue(Double.POSITIVE_INFINITY);
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        assertAllValuesAreSafe();
    }

    @Test
    public void replacesNotANumberInsteadOfAllowingItIntoRobotMath() {
        setEveryNumericValue(Double.NaN);
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        assertAllValuesAreSafe();
    }

    @Test
    public void preservesValidBoundaryValuesExactly() {
        RobotConfig.Drive.STICK_DEADBAND = 0.95;
        RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT = 0;
        RobotConfig.Drive.PRECISION_SCALE = 1;
        RobotConfig.Drive.PINPOINT_X_OFFSET_IN = -123.5;
        RobotConfig.Drive.PINPOINT_Y_OFFSET_IN = 456.25;
        RobotConfig.Drive.AUTO_PATH_SPEED_LIMIT = 1;
        RobotConfig.Intake.COLLECT_POWER = -1;
        RobotConfig.Intake.REVERSE_POWER = 1;
        RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM =
                RobotConfig.Shooter.SHOOTER_MOTOR_MAX_RPM;
        RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE = 0;
        RobotConfig.Shooter.FAR_SHOT_RPM_SCALE = 0;
        RobotConfig.Shooter.SHOOTER_MAX_READY_ERROR_RPM = 0;
        RobotConfig.Shooter.READY_HOLD_SECONDS = 0;
        RobotConfig.Shooter.FEED_POWER = -1;
        RobotConfig.Shooter.FEED_SECONDS = 0;
        RobotConfig.Shooter.HOOD_STOW = 0;
        RobotConfig.Shooter.HOOD_NEAR = 1;
        RobotConfig.Shooter.HOOD_FAR = 0;
        RobotConfig.Shooter.NEAR_DISTANCE_IN = 0;
        RobotConfig.Shooter.FAR_DISTANCE_IN = 0;
        RobotConfig.Vision.MIN_CONFIDENCE = 1;
        RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES = -360;
        RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES = 0;
        RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE = 0;
        RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER = 0;
        RobotConfig.Vision.MAX_FRAME_AGE_MS = 1000;
        RobotConfig.Vision.HIVE_MAX_POSITION_DIFFERENCE_IN = 0;
        RobotConfig.Vision.HIVE_MAX_ROTATION_DIFFERENCE_DEGREES = 180;
        RobotConfig.Vision.HIVE_UPRIGHT_MARGIN_DEGREES = 90;
        RobotConfig.Vision.HIVE_POST_FEED_WAIT_SECONDS = 0;
        RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS = 30;
        RobotConfig.Auto.SHOOT_CUTOFF_SECONDS = 30;

        RobotConfig.keepLiveTuningValuesWithinSafeRanges();

        assertEquals(.95, RobotConfig.Drive.STICK_DEADBAND, 0);
        assertEquals(-123.5, RobotConfig.Drive.PINPOINT_X_OFFSET_IN, 0);
        assertEquals(456.25, RobotConfig.Drive.PINPOINT_Y_OFFSET_IN, 0);
        assertEquals(-1, RobotConfig.Intake.COLLECT_POWER, 0);
        assertEquals(RobotConfig.Shooter.SHOOTER_MOTOR_MAX_RPM,
                RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM, 0);
        assertEquals(-1, RobotConfig.Shooter.FEED_POWER, 0);
        assertEquals(1, RobotConfig.Shooter.HOOD_NEAR, 0);
        assertEquals(-360, RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES, 0);
        assertEquals(1000, RobotConfig.Vision.MAX_FRAME_AGE_MS, 0);
        assertEquals(180, RobotConfig.Vision.HIVE_MAX_ROTATION_DIFFERENCE_DEGREES, 0);
        assertEquals(90, RobotConfig.Vision.HIVE_UPRIGHT_MARGIN_DEGREES, 0);
        assertEquals(30, RobotConfig.Auto.SHOOT_CUTOFF_SECONDS, 0);
    }

    @Test
    public void shootCutoffCanNeverExceedMatchSafetyCutoff() {
        RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS = 12.5;
        RobotConfig.Auto.SHOOT_CUTOFF_SECONDS = 29;
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        assertEquals(12.5, RobotConfig.Auto.SHOOT_CUTOFF_SECONDS, 0);

        RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS = -10;
        RobotConfig.Auto.SHOOT_CUTOFF_SECONDS = 10;
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        assertEquals(0, RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS, 0);
        assertEquals(0, RobotConfig.Auto.SHOOT_CUTOFF_SECONDS, 0);
    }

    @Test
    public void validationIsIdempotent() {
        setEveryNumericValue(Double.POSITIVE_INFINITY);
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        ConfigSnapshot afterFirstValidation = new ConfigSnapshot();
        RobotConfig.keepLiveTuningValuesWithinSafeRanges();
        afterFirstValidation.assertMatchesCurrent();
    }

    private static void setEveryNumericValue(double value) {
        RobotConfig.Drive.STICK_DEADBAND = value;
        RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT = value;
        RobotConfig.Drive.PRECISION_SCALE = value;
        RobotConfig.Drive.PINPOINT_X_OFFSET_IN = value;
        RobotConfig.Drive.PINPOINT_Y_OFFSET_IN = value;
        RobotConfig.Drive.AUTO_PATH_SPEED_LIMIT = value;
        RobotConfig.Intake.COLLECT_POWER = value;
        RobotConfig.Intake.REVERSE_POWER = value;
        RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM = value;
        RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE = value;
        RobotConfig.Shooter.FAR_SHOT_RPM_SCALE = value;
        RobotConfig.Shooter.SHOOTER_MAX_READY_ERROR_RPM = value;
        RobotConfig.Shooter.READY_HOLD_SECONDS = value;
        RobotConfig.Shooter.FEED_POWER = value;
        RobotConfig.Shooter.FEED_SECONDS = value;
        RobotConfig.Shooter.HOOD_STOW = value;
        RobotConfig.Shooter.HOOD_NEAR = value;
        RobotConfig.Shooter.HOOD_FAR = value;
        RobotConfig.Shooter.NEAR_DISTANCE_IN = value;
        RobotConfig.Shooter.FAR_DISTANCE_IN = value;
        RobotConfig.Vision.MIN_CONFIDENCE = value;
        RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES = value;
        RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES = value;
        RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE = value;
        RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER = value;
        RobotConfig.Vision.MAX_FRAME_AGE_MS = value;
        RobotConfig.Vision.HIVE_MAX_POSITION_DIFFERENCE_IN = value;
        RobotConfig.Vision.HIVE_MAX_ROTATION_DIFFERENCE_DEGREES = value;
        RobotConfig.Vision.HIVE_UPRIGHT_MARGIN_DEGREES = value;
        RobotConfig.Vision.HIVE_POST_FEED_WAIT_SECONDS = value;
        RobotConfig.Auto.SHOOT_CUTOFF_SECONDS = value;
        RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS = value;
    }

    private static void assertAllValuesAreSafe() {
        assertRange(RobotConfig.Drive.STICK_DEADBAND, 0, .95);
        assertRange(RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT, 0, 1);
        assertRange(RobotConfig.Drive.PRECISION_SCALE, 0, 1);
        assertTrue(Double.isFinite(RobotConfig.Drive.PINPOINT_X_OFFSET_IN));
        assertTrue(Double.isFinite(RobotConfig.Drive.PINPOINT_Y_OFFSET_IN));
        assertRange(RobotConfig.Drive.AUTO_PATH_SPEED_LIMIT, 0, 1);
        assertRange(RobotConfig.Intake.COLLECT_POWER, -1, 1);
        assertRange(RobotConfig.Intake.REVERSE_POWER, -1, 1);
        assertRange(RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM,
                0, RobotConfig.Shooter.SHOOTER_MOTOR_MAX_RPM);
        assertNonnegativeFinite(RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE);
        assertNonnegativeFinite(RobotConfig.Shooter.FAR_SHOT_RPM_SCALE);
        assertNonnegativeFinite(RobotConfig.Shooter.SHOOTER_MAX_READY_ERROR_RPM);
        assertNonnegativeFinite(RobotConfig.Shooter.READY_HOLD_SECONDS);
        assertRange(RobotConfig.Shooter.FEED_POWER, -1, 1);
        assertNonnegativeFinite(RobotConfig.Shooter.FEED_SECONDS);
        assertRange(RobotConfig.Shooter.HOOD_STOW, 0, 1);
        assertRange(RobotConfig.Shooter.HOOD_NEAR, 0, 1);
        assertRange(RobotConfig.Shooter.HOOD_FAR, 0, 1);
        assertNonnegativeFinite(RobotConfig.Shooter.NEAR_DISTANCE_IN);
        assertNonnegativeFinite(RobotConfig.Shooter.FAR_DISTANCE_IN);
        assertRange(RobotConfig.Vision.MIN_CONFIDENCE, 0, 1);
        assertTrue(Double.isFinite(RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES));
        assertNonnegativeFinite(RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES);
        assertNonnegativeFinite(RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE);
        assertRange(RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER, 0, 1);
        assertRange(RobotConfig.Vision.MAX_FRAME_AGE_MS, 1, 1000);
        assertNonnegativeFinite(RobotConfig.Vision.HIVE_MAX_POSITION_DIFFERENCE_IN);
        assertRange(RobotConfig.Vision.HIVE_MAX_ROTATION_DIFFERENCE_DEGREES, 0, 180);
        assertRange(RobotConfig.Vision.HIVE_UPRIGHT_MARGIN_DEGREES, 0, 90);
        assertNonnegativeFinite(RobotConfig.Vision.HIVE_POST_FEED_WAIT_SECONDS);
        assertRange(RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS, 0, 30);
        assertRange(RobotConfig.Auto.SHOOT_CUTOFF_SECONDS,
                0, RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS);
    }

    private static void assertRange(double value, double minimum, double maximum) {
        assertTrue("not finite: " + value, Double.isFinite(value));
        assertTrue(value + " below " + minimum, value >= minimum);
        assertTrue(value + " above " + maximum, value <= maximum);
    }

    private static void assertNonnegativeFinite(double value) {
        assertRange(value, 0, Double.MAX_VALUE);
    }

    /** Captures every mutable numeric setting so tests cannot leak settings into one another. */
    private static final class ConfigSnapshot {
        private final double[] values = {
                RobotConfig.Drive.STICK_DEADBAND,
                RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT,
                RobotConfig.Drive.PRECISION_SCALE,
                RobotConfig.Drive.PINPOINT_X_OFFSET_IN,
                RobotConfig.Drive.PINPOINT_Y_OFFSET_IN,
                RobotConfig.Drive.AUTO_PATH_SPEED_LIMIT,
                RobotConfig.Intake.COLLECT_POWER,
                RobotConfig.Intake.REVERSE_POWER,
                RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM,
                RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE,
                RobotConfig.Shooter.FAR_SHOT_RPM_SCALE,
                RobotConfig.Shooter.SHOOTER_MAX_READY_ERROR_RPM,
                RobotConfig.Shooter.READY_HOLD_SECONDS,
                RobotConfig.Shooter.FEED_POWER,
                RobotConfig.Shooter.FEED_SECONDS,
                RobotConfig.Shooter.HOOD_STOW,
                RobotConfig.Shooter.HOOD_NEAR,
                RobotConfig.Shooter.HOOD_FAR,
                RobotConfig.Shooter.NEAR_DISTANCE_IN,
                RobotConfig.Shooter.FAR_DISTANCE_IN,
                RobotConfig.Vision.MIN_CONFIDENCE,
                RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES,
                RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES,
                RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE,
                RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER,
                RobotConfig.Vision.MAX_FRAME_AGE_MS,
                RobotConfig.Vision.HIVE_MAX_POSITION_DIFFERENCE_IN,
                RobotConfig.Vision.HIVE_MAX_ROTATION_DIFFERENCE_DEGREES,
                RobotConfig.Vision.HIVE_UPRIGHT_MARGIN_DEGREES,
                RobotConfig.Vision.HIVE_POST_FEED_WAIT_SECONDS,
                RobotConfig.Auto.SHOOT_CUTOFF_SECONDS,
                RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS
        };

        private void restore() {
            int i = 0;
            RobotConfig.Drive.STICK_DEADBAND = values[i++];
            RobotConfig.Drive.NORMAL_DRIVE_POWER_LIMIT = values[i++];
            RobotConfig.Drive.PRECISION_SCALE = values[i++];
            RobotConfig.Drive.PINPOINT_X_OFFSET_IN = values[i++];
            RobotConfig.Drive.PINPOINT_Y_OFFSET_IN = values[i++];
            RobotConfig.Drive.AUTO_PATH_SPEED_LIMIT = values[i++];
            RobotConfig.Intake.COLLECT_POWER = values[i++];
            RobotConfig.Intake.REVERSE_POWER = values[i++];
            RobotConfig.Shooter.SHOOTER_BASE_TARGET_RPM = values[i++];
            RobotConfig.Shooter.NEAR_SHOT_RPM_SCALE = values[i++];
            RobotConfig.Shooter.FAR_SHOT_RPM_SCALE = values[i++];
            RobotConfig.Shooter.SHOOTER_MAX_READY_ERROR_RPM = values[i++];
            RobotConfig.Shooter.READY_HOLD_SECONDS = values[i++];
            RobotConfig.Shooter.FEED_POWER = values[i++];
            RobotConfig.Shooter.FEED_SECONDS = values[i++];
            RobotConfig.Shooter.HOOD_STOW = values[i++];
            RobotConfig.Shooter.HOOD_NEAR = values[i++];
            RobotConfig.Shooter.HOOD_FAR = values[i++];
            RobotConfig.Shooter.NEAR_DISTANCE_IN = values[i++];
            RobotConfig.Shooter.FAR_DISTANCE_IN = values[i++];
            RobotConfig.Vision.MIN_CONFIDENCE = values[i++];
            RobotConfig.Vision.HIVE_AIM_BEARING_DEGREES = values[i++];
            RobotConfig.Vision.HIVE_AIM_TOLERANCE_DEGREES = values[i++];
            RobotConfig.Vision.HIVE_AIM_TURN_POWER_PER_DEGREE = values[i++];
            RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER = values[i++];
            RobotConfig.Vision.MAX_FRAME_AGE_MS = values[i++];
            RobotConfig.Vision.HIVE_MAX_POSITION_DIFFERENCE_IN = values[i++];
            RobotConfig.Vision.HIVE_MAX_ROTATION_DIFFERENCE_DEGREES = values[i++];
            RobotConfig.Vision.HIVE_UPRIGHT_MARGIN_DEGREES = values[i++];
            RobotConfig.Vision.HIVE_POST_FEED_WAIT_SECONDS = values[i++];
            RobotConfig.Auto.SHOOT_CUTOFF_SECONDS = values[i++];
            RobotConfig.Auto.MATCH_SAFETY_CUTOFF_SECONDS = values[i];
        }

        private void assertMatchesCurrent() {
            ConfigSnapshot current = new ConfigSnapshot();
            assertEquals(values.length, current.values.length);
            for (int i = 0; i < values.length; i++) {
                assertEquals("configuration index " + i, values[i], current.values[i], 0);
            }
        }
    }
}

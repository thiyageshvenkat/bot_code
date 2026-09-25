package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.firstinspires.ftc.teamcode.vision.BiobuzzVisionTest.opening;
import static org.firstinspires.ftc.teamcode.vision.BiobuzzVisionTest.pose;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.junit.Test;

public class HiveAimTest {
    @Test
    public void requiresFreshAlignedFramesThroughoutTheHold() {
        HiveAim aim = new HiveAim();
        assertFalse(aim.readyToFeed(opening(30, 0, 100), 0));
        assertFalse(aim.readyToFeed(opening(33, 0, 200), .1));
        assertFalse(aim.readyToFeed(opening(31, 0, 300), .2));
        assertFalse(aim.readyToFeed(opening(32, 0, 400), .3));
        assertTrue(aim.readyToFeed(opening(30, 0, 500), .4));
    }

    @Test
    public void oneFrozenFrameCannotSatisfyTheHold() {
        HiveAim aim = new HiveAim();
        for (int i = 0; i < 10; i++) {
            assertFalse(aim.readyToFeed(opening(30, 0, 100), i * .1));
        }
    }

    @Test
    public void tippingWithoutAnyHorizontalBearingChangeRestartsQualification() {
        HiveAim aim = new HiveAim();
        for (int i = 0; i < 10; i++) {
            double angle = i;
            double sine = Math.sin(Math.toRadians(angle));
            double cosine = Math.cos(Math.toRadians(angle));
            BiobuzzVision.HiveTarget target = BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                    pose(-6.5, -10 + cosine * 7.1874 + sine * 5.622,
                            60 + sine * 7.1874 - cosine * 5.622, 0, 0, angle), 100 + i * 100);
            assertFalse(aim.readyToFeed(target, i * .1));
        }
    }

    @Test
    public void slowlyAccumulatingOpeningMovementCannotPass() {
        HiveAim aim = new HiveAim();
        for (int i = 0; i < 10; i++) {
            assertFalse(aim.readyToFeed(opening(30, .21 * i, 100 + i * 100), i * .1));
        }
    }

    @Test
    public void targetLossCellChangeAndCameraRestartEachRestartTheHold() {
        HiveAim aim = new HiveAim();
        aim.readyToFeed(opening(30, 0, 100), 0);
        aim.readyToFeed(opening(30, 0, 200), .1);
        assertFalse(aim.readyToFeed(null, .2));
        assertFalse(aim.readyToFeed(opening(30, 0, 300), .3));
        assertFalse(aim.readyToFeed(opening(34, 0, 400), .4));
        assertFalse(aim.readyToFeed(opening(34, 0, 10), .5));
        assertFalse(aim.readyToFeed(opening(34, 0, 110), .6));
    }

    @Test
    public void aLongFrameGapOrLostAlignmentCannotCountAsStableTime() {
        HiveAim aim = new HiveAim();
        aim.readyToFeed(opening(30, 0, 100), 0);
        assertFalse(aim.readyToFeed(opening(30, 0, 1000), 1));
        assertFalse(aim.readyToFeed(opening(30, 10, 1100), 1.1));
        assertFalse(aim.readyToFeed(opening(30, 0, 1200), 1.2));
        assertFalse(aim.readyToFeed(opening(30, 0, 1300), 1.3));
    }

    @Test
    public void resetAfterAShotRequiresAnotherFullInterval() {
        HiveAim aim = new HiveAim();
        for (int i = 0; i < 4; i++) {
            aim.readyToFeed(opening(30, 0, 100 + i * 100), i * .1);
        }
        assertTrue(aim.readyToFeed(opening(30, 0, 500), .4));
        aim.reset();
        assertFalse(aim.readyToFeed(opening(30, 0, 500), .4));
        assertFalse(aim.readyToFeed(opening(30, 0, 600), .5));
    }

    @Test
    public void turnsTowardTheTargetAndLimitsPower() {
        assertTrue(HiveAim.turnPower(10) < 0);
        assertTrue(HiveAim.turnPower(-10) > 0);
        assertTrue(Math.abs(HiveAim.turnPower(180)) <= RobotConfig.Vision.HIVE_AIM_MAX_TURN_POWER);
    }
}

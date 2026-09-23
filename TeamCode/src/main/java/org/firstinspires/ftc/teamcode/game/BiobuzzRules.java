package org.firstinspires.ftc.teamcode.game;

import org.firstinspires.ftc.teamcode.constants.MagazineConfig;

/** Pure rule gates used by both TeleOp and autonomous coordination. */
public final class BiobuzzRules {
    public static final double AUTO_SECONDS = 30.0;
    public static final double TELEOP_SECONDS = 120.0;
    public static final double FLOWER_UNLOCK_REMAINING_SECONDS = 60.0;

    private BiobuzzRules() {}

    public static boolean canControlAnother(int controlledCount) {
        return controlledCount >= 0 && controlledCount < MagazineConfig.CAPACITY;
    }

    public static boolean canCollect(AllianceColor alliance, ScoringElement element,
                                     int controlledCount) {
        return element != null
                && element.belongsTo(alliance)
                && canControlAnother(controlledCount);
    }

    public static boolean flowerScoringUnlocked(double teleopElapsedSeconds) {
        return teleopElapsedSeconds >= TELEOP_SECONDS - FLOWER_UNLOCK_REMAINING_SECONDS;
    }

    public static boolean canPlaceInFlower(ScoringElement element,
                                           double teleopElapsedSeconds) {
        return element != null
                && flowerScoringUnlocked(teleopElapsedSeconds);
    }

    public static boolean canRetrieveFromFlower(ScoringElement element) {
        return element == ScoringElement.POLLEN;
    }
}

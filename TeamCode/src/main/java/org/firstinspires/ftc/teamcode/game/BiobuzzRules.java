package org.firstinspires.ftc.teamcode.game;

/** Pure rule gates used by both TeleOp and autonomous coordination. */
public final class BiobuzzRules {
    public static final double AUTO_SECONDS = 30.0;
    public static final double TELEOP_SECONDS = 120.0;
    public static final int MAX_CONTROLLED_ELEMENTS = 4;

    private BiobuzzRules() {}

    public static boolean canControlAnother(int controlledCount) {
        return controlledCount >= 0 && controlledCount < MAX_CONTROLLED_ELEMENTS;
    }

    public static boolean canCollect(AllianceColor alliance, ScoringElement element,
                                     int controlledCount) {
        return element != null
                && element.belongsTo(alliance)
                && canControlAnother(controlledCount);
    }
}

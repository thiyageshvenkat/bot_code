package org.firstinspires.ftc.teamcode.game;

public enum ScoringElement {
    POLLEN,
    RED_NECTAR,
    BLUE_NECTAR;

    boolean belongsTo(AllianceColor alliance) {
        return this == POLLEN
                || alliance == AllianceColor.RED && this == RED_NECTAR
                || alliance == AllianceColor.BLUE && this == BLUE_NECTAR;
    }
}

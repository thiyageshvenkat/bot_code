package org.firstinspires.ftc.teamcode.constants;

/** Flower lift/gate hardware and travel limits. */
public final class FlowerConfig {
    private FlowerConfig() {}

    public static final String LIFT = "flower_lift";
    public static final String GATE = "flower_gate";
    public static final String BOTTOM_LIMIT = "flower_bottom_limit";
    public static final String TOP_LIMIT = "flower_top_limit";
    public static double LIFT_UP_POWER = 0.85;
    public static double LIFT_DOWN_POWER = -0.55;
    public static int SCORE_TICKS = 1200; // TUNE
    public static int POSITION_TOLERANCE_TICKS = 35;
    public static double GATE_CLOSED = 0.14; // TUNE
    public static double GATE_OPEN = 0.76; // TUNE
}

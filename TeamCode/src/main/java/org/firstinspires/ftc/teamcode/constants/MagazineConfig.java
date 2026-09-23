package org.firstinspires.ftc.teamcode.constants;

/** Four-element magazine hardware and indexing tuning. */
public final class MagazineConfig {
    private MagazineConfig() {}

    public static final String GATE = "magazine_gate";
    public static final String EXIT_BEAM = "magazine_exit_beam";
    public static final int CAPACITY = 4; // G407
    public static double GATE_CLOSED = 0.18; // TUNE
    public static double GATE_FEED = 0.72; // TUNE
    public static double FEED_SECONDS = 0.20;
    public static boolean EXIT_BEAM_ACTIVE_LOW = true;
}

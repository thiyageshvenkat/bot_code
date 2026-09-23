package org.firstinspires.ftc.teamcode.constants;

/** Intake hardware and jam-detection tuning. */
public final class IntakeConfig {
    private IntakeConfig() {}

    public static final String MOTOR = "intake";
    public static final String BEAM = "intake_beam";
    public static double COLLECT_POWER = 1.0;
    public static double REVERSE_POWER = -0.75;
    public static double JAM_CURRENT_AMPS = 7.0; // TUNE
    public static double JAM_TIME_SECONDS = 0.30;
    public static double CLEAR_TIME_SECONDS = 0.22;
    public static boolean BEAM_ACTIVE_LOW = true;
}

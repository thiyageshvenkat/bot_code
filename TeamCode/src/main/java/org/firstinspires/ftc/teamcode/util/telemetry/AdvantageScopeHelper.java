package org.firstinspires.ftc.teamcode.util.telemetry;

import com.pedropathing.math.Pose;

import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.mechanism.LoggedMechanism2d;
import org.psilynx.psikit.core.rlog.RLOGServer;
import org.psilynx.psikit.core.wpi.StructSerializable;
import org.psilynx.psikit.core.wpi.WPISerializable;
import org.psilynx.psikit.core.wpi.math.Pose2d;
import org.psilynx.psikit.core.wpi.math.Rotation2d;
import org.psilynx.psikit.core.wpi.math.Translation2d;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * A class with static methods that streamline AdvantageScope usage.
 */
public class AdvantageScopeHelper {

    private AdvantageScopeHelper() {}

    // ========= Logger "passthrough" methods ===========


    public static void init(int port) {
        RLOGServer server = new RLOGServer(port);
        Logger.addDataReceiver(server);
    }

    /**
     * Call this after waitForStart().
     */
    public static void start() {
        Logger.start();
        periodicBeforeUser();
    }

    public static void end() {
        Logger.end();
        shouldRunBeforeUserPeriodic = true;
    }

    private static boolean shouldRunBeforeUserPeriodic = true;
    public static void periodicBeforeUser() {
        if (!shouldRunBeforeUserPeriodic) return;

        Logger.periodicBeforeUser();
        shouldRunBeforeUserPeriodic = false;
    }

    public static void periodicAfterUser(double userCodeLength, double periodicAfterLength) {
        if (shouldRunBeforeUserPeriodic) return;

        Logger.periodicAfterUser(userCodeLength, periodicAfterLength);
        shouldRunBeforeUserPeriodic = true;
    }


    // ========= Helper Methods ===============

    @SuppressWarnings("rawtypes")
    public static void recordOutput(String key, Object value) {
        if (value == null) return;

        // Primitives & Common Types (Ordered by highest expected frequency)
        if (value instanceof Double) {
            Logger.recordOutput(key, (double) value);
        } else if (value instanceof Integer) {
            Logger.recordOutput(key, (int) value);
        } else if (value instanceof Boolean) {
            Logger.recordOutput(key, (boolean) value);
        } else if (value instanceof double[]) {
            Logger.recordOutput(key, (double[]) value);
        } else if (value instanceof String) {
            Logger.recordOutput(key, (String) value);
        } else if (value instanceof int[]) {
            Logger.recordOutput(key, (int[]) value);
        } else if (value instanceof boolean[]) {
            Logger.recordOutput(key, (boolean[]) value);


        } else if (value instanceof Enum) {
            logRawEnum(key, (Enum) value);
        } else if (value instanceof Enum[]) {
            logRawEnumArray(key, (Enum[]) value);
        } else if (value instanceof Enum[][]) {
            logRawEnumMatrix(key, (Enum[][]) value);

            // FRC / AdvantageKit Specific Objects
        } else if (value instanceof WPISerializable) {
            Logger.recordOutput(key, (WPISerializable) value);
        } else if (value instanceof LoggedMechanism2d) {
            Logger.recordOutput(key, (LoggedMechanism2d) value);

            // Suppliers (Functional interfaces)
        } else if (value instanceof DoubleSupplier) {
            Logger.recordOutput(key, (DoubleSupplier) value);
        } else if (value instanceof IntSupplier) {
            Logger.recordOutput(key, (IntSupplier) value);
        } else if (value instanceof BooleanSupplier) {
            Logger.recordOutput(key, (BooleanSupplier) value);
        } else if (value instanceof LongSupplier) {
            Logger.recordOutput(key, (LongSupplier) value);

            // Other Primitives / Objects
        } else if (value instanceof Long) {
            Logger.recordOutput(key, (long) value);
        } else if (value instanceof Float) {
            Logger.recordOutput(key, (float) value);
        } else if (value instanceof byte[]) {
            Logger.recordOutput(key, (byte[]) value);
        } else if (value instanceof String[]) {
            Logger.recordOutput(key, (String[]) value);
        } else if (value instanceof long[]) {
            Logger.recordOutput(key, (long[]) value);
        } else if (value instanceof float[]) {
            Logger.recordOutput(key, (float[]) value);

            // 2D Arrays
        } else if (value instanceof double[][]) {
            Logger.recordOutput(key, (double[][]) value);
        } else if (value instanceof int[][]) {
            Logger.recordOutput(key, (int[][]) value);
        } else if (value instanceof boolean[][]) {
            Logger.recordOutput(key, (boolean[][]) value);
        } else if (value instanceof String[][]) {
            Logger.recordOutput(key, (String[][]) value);
        } else if (value instanceof byte[][]) {
            Logger.recordOutput(key, (byte[][]) value);
        } else if (value instanceof long[][]) {
            Logger.recordOutput(key, (long[][]) value);
        } else if (value instanceof float[][]) {
            Logger.recordOutput(key, (float[][]) value);
        } else if (value instanceof StructSerializable[]) {
            Logger.recordOutput(key, (StructSerializable[]) value);
        } else if (value instanceof StructSerializable[][]) {
            Logger.recordOutput(key, (StructSerializable[][]) value);

        } else {

            // Fallback or warning if an unsupported type is passed
            System.out.println("Unknown type passed to helper: " + value.getClass().getName());
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void logRawEnum(String key, Enum value) {
        Logger.recordOutput(key, value);
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void logRawEnumArray(String key, Enum[] value) {
        Logger.recordOutput(key, value);
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void logRawEnumMatrix(String key, Enum[][] value) {
        Logger.recordOutput(key, value);
    }


    /**
     * Records a default PedroPathing Pose object to AdvantageScope. The pose is converted to a
     * {@link Pose2d} object with the origin shifted by (-72, -72) inches.
     * @param pathAndCaption The path and caption for the AdvantageScope output.
     * @param pose The PedroPathing Pose object to be recorded.
     */
    public static void recordPose(String pathAndCaption, Pose pose) {
        Logger.recordOutput(
                pathAndCaption,
                new Pose2d(
                        new Translation2d(
                                pose.x() - 72,
                                pose.y() - 72
                        ),
                        new Rotation2d(pose.heading())
                )
        );
    }

}

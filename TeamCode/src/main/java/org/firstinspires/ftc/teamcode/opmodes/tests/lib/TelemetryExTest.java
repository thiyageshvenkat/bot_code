package org.firstinspires.ftc.teamcode.opmodes.tests.lib;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.util.telemetry.TelemetryEx;

@Utility(name = "TelemetryEx Test", description = "Test the functionality of the TelemetryEx class.")
@Disabled
public class TelemetryExTest extends LinearOpMode {

    private static final int MIN_LINES = 100;
    private static final int MAX_LINES = 1000;

    // A very high frequency means TelemetryEx will update whenever the
    // FTC OpMode loop gets around to calling requestUpdate().
    private static final double TELEMETRY_EX_FREQUENCY = 25;

    private enum TelemetryMode {
        NORMAL,
        EXTENDED
    }

    @Override
    public void runOpMode() {

        TelemetryEx telemetryEx = TelemetryEx.getInstance();
        telemetryEx.init(telemetry, TELEMETRY_EX_FREQUENCY);

        TelemetryMode mode = TelemetryMode.NORMAL;

        int dataLines = 10;

        // Button edge detection
        boolean previousA = false;
        boolean previousDpadUp = false;
        boolean previousDpadDown = false;

        // Loop timing
        ElapsedTime loopTimer = new ElapsedTime();

        double loopTimeMs = 0;
        double loopRateHz = 0;

        // Running statistics
        double totalLoopTimeMs = 0;
        long loopCount = 0;

        // Timing of the telemetry update itself
        double telemetryUpdateTimeMs = 0;

        // Rolling average over the most recent loops
        final int WINDOW_SIZE = 100;
        double[] loopTimes = new double[WINDOW_SIZE];
        int windowIndex = 0;
        int windowCount = 0;

        telemetry.addLine("TelemetryEx Test");
        telemetry.addLine("");
        telemetry.addLine("A = Switch telemetry mode");
        telemetry.addLine("DPad Up/Down = Change data lines");
        telemetry.addLine("");
        telemetry.addLine("Press PLAY to begin.");

        telemetry.update();

        waitForStart();

        loopTimer.reset();

        while (opModeIsActive()) {

            // ============================================================
            // LOOP TIMING
            // ============================================================

            double currentLoopTimeMs = loopTimer.milliseconds();
            loopTimer.reset();

            loopTimeMs = currentLoopTimeMs;

            if (loopTimeMs > 0) {
                loopRateHz = 1000.0 / loopTimeMs;
            }

            totalLoopTimeMs += loopTimeMs;
            loopCount++;

            // Rolling average
            loopTimes[windowIndex] = loopTimeMs;
            windowIndex = (windowIndex + 1) % WINDOW_SIZE;

            if (windowCount < WINDOW_SIZE) {
                windowCount++;
            }

            double rollingAverageMs = 0;

            for (int i = 0; i < windowCount; i++) {
                rollingAverageMs += loopTimes[i];
            }

            rollingAverageMs /= windowCount;

            double rollingRateHz = 0;

            if (rollingAverageMs > 0) {
                rollingRateHz = 1000.0 / rollingAverageMs;
            }

            double overallAverageMs = totalLoopTimeMs / loopCount;

            double overallRateHz = 0;

            if (overallAverageMs > 0) {
                overallRateHz = 1000.0 / overallAverageMs;
            }

            // ============================================================
            // CONTROLLER INPUT
            // ============================================================

            boolean currentA = gamepad1.a;
            boolean currentDpadUp = gamepad1.dpad_up;
            boolean currentDpadDown = gamepad1.dpad_down;

            // Toggle telemetry mode
            if (currentA && !previousA) {

                if (mode == TelemetryMode.NORMAL) {
                    mode = TelemetryMode.EXTENDED;
                } else {
                    mode = TelemetryMode.NORMAL;
                }
            }

            // Increase number of lines
            if (currentDpadUp && !previousDpadUp) {
                dataLines = Range.clip(dataLines + 1, MIN_LINES, MAX_LINES);
            }

            // Decrease number of lines
            if (currentDpadDown && !previousDpadDown) {
                dataLines = Range.clip(dataLines - 1, MIN_LINES, MAX_LINES);
            }

            previousA = currentA;
            previousDpadUp = currentDpadUp;
            previousDpadDown = currentDpadDown;

            // ============================================================
            // BUILD TELEMETRY
            // ============================================================

            if (mode == TelemetryMode.NORMAL) {

                double startTelemetryTime = loopTimer.milliseconds();

                telemetry.addLine("=== NORMAL TELEMETRY ===");
                telemetry.addData("Lines", dataLines);

                for (int i = 0; i < dataLines; i++) {
                    telemetry.addData("Data " + i, i);
                }

                telemetry.addLine("");
                telemetry.addData("Loop time", "%.3f ms", loopTimeMs);
                telemetry.addData("Loop rate", "%.2f Hz", loopRateHz);
                telemetry.addData("Avg loop", "%.3f ms", overallAverageMs);
                telemetry.addData("Avg rate", "%.2f Hz", overallRateHz);
                telemetry.addData("100-loop avg", "%.3f ms", rollingAverageMs);
                telemetry.addData("100-loop rate", "%.2f Hz", rollingRateHz);

                telemetry.addLine("");
                telemetry.addLine("A = Extended | DPad Up/Down = Lines");

                telemetryUpdateTimeMs = loopTimer.milliseconds() - startTelemetryTime;

                telemetry.update();

            } else {

                double startTelemetryTime = loopTimer.milliseconds();

                telemetryEx.addLine("=== TELEMETRY EX ===");
                telemetryEx.addData("Lines", dataLines);

                for (int i = 0; i < dataLines; i++) {
                    telemetryEx.addData("Data " + i, i);
                }

                telemetryEx.space();
                telemetryEx.addData("Loop time", String.format("%.3f ms", loopTimeMs));
                telemetryEx.addData("Loop rate", String.format("%.2f Hz", loopRateHz));
                telemetryEx.addData("Avg loop", String.format("%.3f ms", overallAverageMs));
                telemetryEx.addData("Avg rate", String.format("%.2f Hz", overallRateHz));
                telemetryEx.addData("100-loop avg", String.format("%.3f ms", rollingAverageMs));
                telemetryEx.addData("100-loop rate", String.format("%.2f Hz", rollingRateHz));

                telemetryEx.space();
                telemetryEx.addLine("A = Normal | DPad Up/Down = Lines");

                telemetryUpdateTimeMs = loopTimer.milliseconds() - startTelemetryTime;

                telemetryEx.requestUpdate();
            }
        }
    }
}
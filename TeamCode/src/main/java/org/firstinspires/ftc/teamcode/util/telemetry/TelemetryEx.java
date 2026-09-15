package org.firstinspires.ftc.teamcode.util.telemetry;


import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.Config;

import java.util.Locale;

public class TelemetryEx {

    // ==========SINGLETON STUFF==========
    private static TelemetryEx INSTANCE;

    public static TelemetryEx getInstance() {
        if (INSTANCE == null) INSTANCE = new TelemetryEx();

        return INSTANCE;
    }

    private TelemetryEx() {}

    // =========ACTUAL STUFF==========

    private Telemetry telemetry; // the actual telemetry object. supplied via init().
    private TelemetryManager panelsTelemetry;
    private double updateWaitMs;
    private final ElapsedTime updateTimer = new ElapsedTime();

    private final StringBuilder outputBuilder = new StringBuilder();

    private String advantageScopePath = "General"; // default path for AdvantageScope output

    public void init(Telemetry telemetry) {
        init(telemetry, 20);
    }

    public void init(Telemetry telemetry, double updateFrequencyHz) {
        this.telemetry = telemetry;
        this.panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        if (Config.USE_ADVANTAGE_SCOPE) {
            AdvantageScopeHelper.init(5800);
        }

        if (!Config.USE_TELEMETRY) return;

        this.updateWaitMs = 1000.0 / updateFrequencyHz;
        telemetry.setMsTransmissionInterval((int) Math.max(updateWaitMs, 10));
        updateTimer.reset();
    }



    public TelemetryEx useAdvantageScopePath(String path) {
        if (Config.USE_ADVANTAGE_SCOPE) {
            advantageScopePath = path.isEmpty() ? "General" : path;
        }
        return this;
    }

    public TelemetryEx addLine(String line) {
        if (Config.USE_TELEMETRY) {
            outputBuilder.append(line)
                    .append("\n");
        }

        return this;
    }

    public TelemetryEx addData(String caption, Object value) {
        if (Config.USE_TELEMETRY) {
            outputBuilder.append(caption)
                    .append(" : ")
                    .append(value)
                    .append("\n");
        }
        if (Config.USE_ADVANTAGE_SCOPE) {
            AdvantageScopeHelper.recordOutput(advantageScopePath + "/" + caption.replace("/", "∕"), value);
        }

        return this;
    }

    public TelemetryEx addData(String caption, String format, Object... values) {
        if (Config.USE_TELEMETRY) {
            outputBuilder.append(caption)
                    .append(" : ")
                    .append(String.format(Locale.US, format, values))
                    .append("\n");
        }

        if (Config.USE_ADVANTAGE_SCOPE) {
            for (int i = 0; i < values.length; i++) {
                AdvantageScopeHelper.recordOutput(
                        advantageScopePath + "/"
                            + caption.replace("/", "∕") + "/"
                            + i,
                        values[i]);
            }
        }

        return this;
    }

    public TelemetryEx space() {
        if (Config.USE_TELEMETRY) {
            outputBuilder.append("\n");
        }

        return this;
    }


    public void requestUpdate() {
        if (Config.USE_ADVANTAGE_SCOPE) {
            AdvantageScopeHelper.periodicAfterUser(0.0, 0.0);
            AdvantageScopeHelper.periodicBeforeUser();
        }

        if (Config.USE_TELEMETRY && telemetry != null) {
            // if it's time to update and we have stuff to update,
            // read the buffer, print it, and then update
            if (updateTimer.milliseconds() >= updateWaitMs && outputBuilder.length() > 0) {
                String output = outputBuilder.toString();
                telemetry.addLine(output);
                telemetry.update();

                if (Config.USE_PANELS) {
                    panelsTelemetry.addLine(output);
                    panelsTelemetry.update();
                }

                updateTimer.reset();
            }

            // always clear the buffer
            outputBuilder.setLength(0);
        }
    }

    public void forceUpdate() {
        if (!Config.USE_TELEMETRY || telemetry == null) return;

        String output = outputBuilder.toString();
        telemetry.addLine(output);
        telemetry.update();

        if (Config.USE_PANELS) {
            panelsTelemetry.addLine(output);
            panelsTelemetry.update();
        }

        updateTimer.reset();
        outputBuilder.setLength(0);
    }

}

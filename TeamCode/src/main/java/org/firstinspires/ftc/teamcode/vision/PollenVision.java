package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/** Minimal adapter for the confirmed Limelight neural pollen detector. */
public final class PollenVision implements AutoCloseable {
    public static final class Target {
        public final double bearingDegrees;
        public final double areaPercent;

        Target(double bearingDegrees, double areaPercent) {
            this.bearingDegrees = bearingDegrees;
            this.areaPercent = areaPercent;
        }
    }

    private final Limelight3A limelight;

    public PollenVision(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, RobotConfig.Vision.LIMELIGHT);
        limelight.setPollRateHz(50);
        limelight.pipelineSwitch(RobotConfig.Vision.POLLEN_PIPELINE);
        limelight.start();
    }

    public Target bestPollen() {
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) return null;
        Target best = null;
        for (LLResultTypes.DetectorResult detection : result.getDetectorResults()) {
            if (!RobotConfig.Vision.POLLEN_CLASS.equals(detection.getClassName())
                    || detection.getConfidence() < RobotConfig.Vision.MIN_CONFIDENCE) continue;
            Target candidate = new Target(detection.getTargetXDegrees(), detection.getTargetArea());
            if (best == null || candidate.areaPercent > best.areaPercent) best = candidate;
        }
        return best;
    }

    public boolean isConnected() { return limelight.isConnected(); }

    @Override
    public void close() { limelight.stop(); }
}

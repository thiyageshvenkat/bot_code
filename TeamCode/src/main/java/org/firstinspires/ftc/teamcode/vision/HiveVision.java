package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/** Relative cell targeting. BIOBUZZ cell tags move, so they are not field anchors. */
public final class HiveVision implements AutoCloseable {
    private final AprilTagProcessor aprilTags;
    private final VisionPortal portal;

    public HiveVision(HardwareMap hardwareMap) {
        aprilTags = new AprilTagProcessor.Builder()
                .setTagLibrary(AprilTagGameDatabase.getBioBuzzTagLibrary())
                .build();
        aprilTags.setDecimation(2);
        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, RobotConfig.Hardware.WEBCAM))
                .addProcessor(aprilTags)
                .build();
    }

    /** Returns the closest visible cell tag/cluster. Driver orientation selects the alliance hive. */
    public HiveObservation closestCell() {
        List<AprilTagDetection> detections = aprilTags.getDetections();
        AprilTagDetection closest = null;
        for (AprilTagDetection detection : detections) {
            if (detection.ftcPose == null || !Double.isFinite(detection.ftcPose.range)) continue;
            if (closest == null || detection.ftcPose.range < closest.ftcPose.range) closest = detection;
        }
        if (closest == null) return null;
        return new HiveObservation(closest.ftcPose.range, closest.ftcPose.bearing,
                closest.ftcPose.yaw, closest.frameAcquisitionNanoTime);
    }

    public double aimTurnPower() {
        HiveObservation observation = closestCell();
        if (observation == null
                || Math.abs(observation.bearingDegrees)
                <= RobotConfig.Vision.AIM_BEARING_TOLERANCE_DEG) return 0.0;
        return Range.clip(observation.bearingDegrees * RobotConfig.Vision.AIM_TURN_KP,
                -RobotConfig.Vision.MAX_AIM_TURN, RobotConfig.Vision.MAX_AIM_TURN);
    }

    public boolean isAligned() {
        HiveObservation observation = closestCell();
        return observation != null
                && Math.abs(observation.bearingDegrees)
                <= RobotConfig.Vision.AIM_BEARING_TOLERANCE_DEG;
    }

    @Override
    public void close() {
        portal.close();
    }
}

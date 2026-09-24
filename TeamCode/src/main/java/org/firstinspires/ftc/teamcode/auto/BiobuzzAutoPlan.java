package org.firstinspires.ftc.teamcode.auto;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.game.AllianceColor;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

/** Red Pedro Path Generator route supplied by the team, mirrored for blue. */
public final class BiobuzzAutoPlan {
    public static final double FIELD_SIZE_IN = 144.0;

    public final Pose start;
    public final Pose southHiveShoot;
    public final Pose gardenCollect;
    public final Pose northApproach;
    public final Pose northCurveExit;
    public final Pose northHiveShoot;
    public final Pose topFlowerCollect;
    public final Pose loadingZonePark;

    public final Path startToSouthHive;
    public final Path southHiveToGarden;
    public final Path gardenToNorthApproach;
    public final Path northCornerCurve;
    public final Path northCurveToHive;
    public final Path northHiveToTopFlower;
    public final Path topFlowerToNorthHive;
    public final Path northHiveToLoadingZone;

    /** Conservative aliases used by the current preload-and-park autonomous. */
    public final Path toShoot;
    public final Path toPark;

    private BiobuzzAutoPlan(AllianceColor alliance) {
        start = forAlliance(alliance, 56, 8, 90);
        southHiveShoot = forAlliance(alliance, 59.25, 42, 90);
        gardenCollect = forAlliance(alliance, 10, 10, 270);
        northApproach = forAlliance(alliance, 31.9496, 92.4893, 270);
        northCurveExit = forAlliance(alliance, 46.6173, 102.9301, 270);
        Pose curveControl1 = forAlliance(alliance, 33.9541, 100.0223, 0);
        Pose curveControl2 = forAlliance(alliance, 38.8433, 103.5025, 0);
        northHiveShoot = forAlliance(alliance, 59.25, 102, 270);
        topFlowerCollect = forAlliance(alliance, 48, 132, 90);
        loadingZonePark = forAlliance(alliance, 8, 108, 180);

        startToSouthHive = straight(start, southHiveShoot);
        southHiveToGarden = straight(southHiveShoot, gardenCollect);
        gardenToNorthApproach = straight(gardenCollect, northApproach);
        northCornerCurve = curve(northApproach, curveControl1, curveControl2, northCurveExit)
                .linear(northApproach, northCurveExit);
        northCurveToHive = straight(northCurveExit, northHiveShoot);
        northHiveToTopFlower = straight(northHiveShoot, topFlowerCollect);
        topFlowerToNorthHive = straight(topFlowerCollect, northHiveShoot);
        northHiveToLoadingZone = straight(northHiveShoot, loadingZonePark);

        toShoot = startToSouthHive;
        toPark = straight(southHiveShoot, loadingZonePark);
    }

    public static BiobuzzAutoPlan forAlliance(AllianceColor alliance) {
        if (alliance == null) throw new IllegalArgumentException("alliance is required");
        return new BiobuzzAutoPlan(alliance);
    }

    private static Path straight(Pose from, Pose to) {
        return line(from, to).linear(from, to);
    }

    private static Pose forAlliance(AllianceColor alliance, double redX, double y,
                                    double redHeadingDegrees) {
        if (alliance == AllianceColor.RED) {
            return degrees(redX, y, redHeadingDegrees);
        }
        return degrees(FIELD_SIZE_IN - redX, y, 180.0 - redHeadingDegrees);
    }

    private static Pose degrees(double x, double y, double headingDegrees) {
        double wrapped = ((headingDegrees % 360.0) + 360.0) % 360.0;
        return new Pose(x, y, Math.toRadians(wrapped));
    }
}

package org.firstinspires.ftc.teamcode.auto;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.game.AllianceColor;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

/**
 * Owns the field geometry imported from the team's Pedro Path Generator file.
 *
 * <p>Red is the canonical version because that is how the supplied route was drawn. Blue uses the
 * same strategy mirrored across the field, which avoids maintaining two nearly identical coordinate
 * lists. Mirroring is only a starting assumption: both alliances still require separate field tests.
 * This class creates geometry only; it does not decide which paths an OpMode will actually run.</p>
 */
public final class BiobuzzAutoPlan {
    // Pedro coordinates cover the complete square field. This value defines the red-to-blue mirror.
    public static final double FIELD_SIZE_IN = 144.0;

    // Named poses make the generated route readable in field terms. They also let tests and future
    // Auto states reuse the original team route without copying coordinates into an OpMode.
    public final Pose start;
    public final Pose southHiveShoot;
    public final Pose gardenCollect;
    public final Pose northApproach;
    public final Pose northCurveExit;
    public final Pose northHiveShoot;
    public final Pose topFlowerCollect;
    public final Pose loadingZonePark;

    // These are all segments supplied by the team, including segments the current conservative
    // preload Auto does not run yet. Keeping them here preserves the full route for later expansion.
    public final Path startToSouthHive;
    public final Path southHiveToGarden;
    public final Path gardenToNorthApproach;
    public final Path northCornerCurve;
    public final Path northCurveToHive;
    public final Path northHiveToTopFlower;
    public final Path topFlowerToNorthHive;
    public final Path northHiveToLoadingZone;

    /**
     * The current competition Auto intentionally runs only the first shooting leg and a direct park.
     * These names keep that limited strategy obvious inside BiobuzzAutoBase without discarding the
     * remaining generated route above.
     */
    public final Path toShoot;
    public final Path toPark;

    /** Builds one alliance's complete route from the canonical red coordinates. */
    private BiobuzzAutoPlan(AllianceColor alliance) {
        // Pose headings describe the robot's desired field orientation, not the direction of travel.
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
        // The control poses shape the corner but are not stopping points the robot must reach.
        northCornerCurve = curve(northApproach, curveControl1, curveControl2, northCurveExit)
                .linear(northApproach, northCurveExit);
        northCurveToHive = straight(northCurveExit, northHiveShoot);
        northHiveToTopFlower = straight(northHiveShoot, topFlowerCollect);
        topFlowerToNorthHive = straight(topFlowerCollect, northHiveShoot);
        northHiveToLoadingZone = straight(northHiveShoot, loadingZonePark);

        // A direct south-Hive-to-park path is separate from the longer supplied north-Hive route.
        toShoot = startToSouthHive;
        toPark = straight(southHiveShoot, loadingZonePark);
    }

    /**
     * Creates a fresh plan for the selected alliance so every path is built from matching poses.
     *
     * @throws IllegalArgumentException when an OpMode fails to declare its alliance
     */
    public static BiobuzzAutoPlan forAlliance(AllianceColor alliance) {
        if (alliance == null) throw new IllegalArgumentException("alliance is required");
        return new BiobuzzAutoPlan(alliance);
    }

    /** Uses a straight geometric segment while interpolating heading between its endpoint poses. */
    private static Path straight(Pose from, Pose to) {
        return line(from, to).linear(from, to);
    }

    /**
     * Reflects red X and heading for blue while preserving Y in the coordinate system used by the
     * supplied Path Generator file. This transform must be verified on the regulation field.
     */
    private static Pose forAlliance(AllianceColor alliance, double redX, double y,
                                    double redHeadingDegrees) {
        if (alliance == AllianceColor.RED) {
            return degrees(redX, y, redHeadingDegrees);
        }
        return degrees(FIELD_SIZE_IN - redX, y, 180.0 - redHeadingDegrees);
    }

    /** Converts readable degree inputs to Pedro's radian pose and normalizes unusual headings. */
    private static Pose degrees(double x, double y, double headingDegrees) {
        double wrapped = ((headingDegrees % 360.0) + 360.0) % 360.0;
        return new Pose(x, y, Math.toRadians(wrapped));
    }
}

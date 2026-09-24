package org.firstinspires.ftc.teamcode.auto;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.game.ElementInventory.AllianceColor;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

/** Red Pedro Path Generator route supplied by the team, mirrored for blue. */
public final class BiobuzzAutoPlan {
    public static final double FIELD_SIZE_IN = 144.0;
    public static final int START = 0, SOUTH_HIVE = 1, GARDEN = 2, NORTH_APPROACH = 3;
    public static final int NORTH_CURVE_EXIT = 4, NORTH_HIVE = 5, TOP_FLOWER = 6, PARK = 7;

    public final Pose[] poses;
    public final Path[] route;

    /** Conservative aliases used by the current preload-and-park autonomous. */
    public final Path toShoot;
    public final Path toPark;

    private BiobuzzAutoPlan(AllianceColor alliance) {
        poses = new Pose[] {
                forAlliance(alliance, 56, 8, 90),
                forAlliance(alliance, 59.25, 42, 90),
                forAlliance(alliance, 10, 10, 270),
                forAlliance(alliance, 31.9496, 92.4893, 270),
                forAlliance(alliance, 46.6173, 102.9301, 270),
                forAlliance(alliance, 59.25, 102, 270),
                forAlliance(alliance, 48, 132, 90),
                forAlliance(alliance, 8, 108, 180)
        };
        Pose curveControl1 = forAlliance(alliance, 33.9541, 100.0223, 0);
        Pose curveControl2 = forAlliance(alliance, 38.8433, 103.5025, 0);
        route = new Path[] {
                straight(poses[START], poses[SOUTH_HIVE]),
                straight(poses[SOUTH_HIVE], poses[GARDEN]),
                straight(poses[GARDEN], poses[NORTH_APPROACH]),
                curve(poses[NORTH_APPROACH], curveControl1, curveControl2,
                        poses[NORTH_CURVE_EXIT]).linear(poses[NORTH_APPROACH],
                        poses[NORTH_CURVE_EXIT]),
                straight(poses[NORTH_CURVE_EXIT], poses[NORTH_HIVE]),
                straight(poses[NORTH_HIVE], poses[TOP_FLOWER]),
                straight(poses[TOP_FLOWER], poses[NORTH_HIVE]),
                straight(poses[NORTH_HIVE], poses[PARK])
        };

        toShoot = route[0];
        toPark = straight(poses[SOUTH_HIVE], poses[PARK]);
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

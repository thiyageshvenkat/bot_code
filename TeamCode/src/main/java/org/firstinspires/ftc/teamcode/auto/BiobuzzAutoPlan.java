package org.firstinspires.ftc.teamcode.auto;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.constants.AutoConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;

import static com.pedropathing.api.Paths.line;

/** Tunable preload-to-hive-to-loading-zone route mirrored by alliance. */
public final class BiobuzzAutoPlan {
    public final Pose start;
    public final Pose shoot;
    public final Pose park;
    public final Path toShoot;
    public final Path toPark;

    private BiobuzzAutoPlan(Pose start, Pose shoot, Pose park) {
        this.start = start;
        this.shoot = shoot;
        this.park = park;
        toShoot = line(start, shoot).linear(start, shoot);
        toPark = line(shoot, park).linear(shoot, park);
    }

    public static BiobuzzAutoPlan forAlliance(AllianceColor alliance) {
        Pose blueStart = degrees(AutoConfig.START_X,
                AutoConfig.START_Y, AutoConfig.START_HEADING_DEG);
        Pose blueShoot = degrees(AutoConfig.SHOOT_X,
                AutoConfig.SHOOT_Y, AutoConfig.SHOOT_HEADING_DEG);
        Pose bluePark = degrees(AutoConfig.PARK_X,
                AutoConfig.PARK_Y, AutoConfig.PARK_HEADING_DEG);
        if (alliance == AllianceColor.BLUE) {
            return new BiobuzzAutoPlan(blueStart, blueShoot, bluePark);
        }
        return new BiobuzzAutoPlan(mirrorAcrossCenterline(blueStart),
                mirrorAcrossCenterline(blueShoot), mirrorAcrossCenterline(bluePark));
    }

    private static Pose degrees(double x, double y, double headingDegrees) {
        return new Pose(x, y, Math.toRadians(headingDegrees));
    }

    private static Pose mirrorAcrossCenterline(Pose blue) {
        return new Pose(-blue.x(), blue.y(), Math.PI - blue.heading());
    }
}

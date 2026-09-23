package org.firstinspires.ftc.teamcode.opmodes.tests.lib;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.util.telemetry.AdvantageScopeHelper;
import org.psilynx.psikit.core.Logger;
// Import the RLOG server module from your specific package
import org.psilynx.psikit.core.rlog.RLOGServer;
import org.psilynx.psikit.core.wpi.math.Pose2d;
import org.psilynx.psikit.core.wpi.math.Rotation2d;
import org.psilynx.psikit.core.wpi.math.Translation2d;

/**
 * AdvantageScopeHelper is already implemented into TelemetryEx, so you don't need to use this class
 * if you are using TelemetryEx. This is just a demonstration of how to use AdvantageScopeHelper
 * with PsiKit's RLOG server for logging and visualization.
 */
@Utility(name = "PsiKit RLOG Graph", description = "Demonstrates how to use PsiKit's RLOG server for logging and visualization.")
public class PsiKitDemo extends LinearOpMode {

    @Override
    public void runOpMode() {

        // 1. Fire up the custom background server port (Port 5800)
        AdvantageScopeHelper.init(5800);

        telemetry.addLine("Status : Initialized. Run OpMode & select RLOG in AScope!");
        telemetry.update();

        waitForStart();
        // 0. Call AdvantageScopeHelper.start() AFTER waitForStart() to begin logging data to the RLOG server
        AdvantageScopeHelper.start();


        while (opModeIsActive()) {
            double timeSeconds = getRuntime();
            double simulatedData = Math.sin(timeSeconds);

            AdvantageScopeHelper.recordOutput("Demo/SineWave", simulatedData);
            AdvantageScopeHelper.recordOutput("Demo/Time", timeSeconds);
            AdvantageScopeHelper.recordPose("Demo/RobotPose", new Pose(
                            72 + 72 * Math.cos(timeSeconds),
                            72 + 72 * Math.sin(timeSeconds),
                            -timeSeconds
                    )
            );

            telemetry.addData("Data Sent", simulatedData);
            telemetry.update();

            // periodicBeforeUser() is called at the end because start() already calls it, so it
            // would be called twice if it were at the beginning.
            AdvantageScopeHelper.periodicAfterUser(0.0, 0.0);
            AdvantageScopeHelper.periodicBeforeUser();
        }


        // Clean up server when OpMode finishes
        AdvantageScopeHelper.end();
    }
}

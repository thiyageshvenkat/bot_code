package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;

/**
 * Checks that an autonomous OpMode can move the four-motor drivebase without Pinpoint.
 * This is a timed forward motor test, not position tracking or Pedro path following.
 */
@Autonomous(name = "Drivebase Forward Auto Test", group = "Testing")
public final class DrivebaseForwardAutoTest extends LinearOpMode {
    // Low power limits how quickly an incorrect motor direction can move the robot.
    private static final double FORWARD_POWER = 0.15;
    // A short pulse is long enough to see all four wheels contribute to forward movement.
    private static final long DRIVE_TIME_MILLISECONDS = 500;

    @Override
    public void runOpMode() {
        DcMotor frontLeft = hardwareMap.get(DcMotor.class, RobotConfig.Drive.FRONT_LEFT);
        DcMotor frontRight = hardwareMap.get(DcMotor.class, RobotConfig.Drive.FRONT_RIGHT);
        DcMotor backLeft = hardwareMap.get(DcMotor.class, RobotConfig.Drive.BACK_LEFT);
        DcMotor backRight = hardwareMap.get(DcMotor.class, RobotConfig.Drive.BACK_RIGHT);

        // These directions match the direct mecanum TeleOp already verified on this robot.
        frontLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addLine("No Pinpoint is required for this test");
        telemetry.addLine("On START: drive forward at 15% power for 0.5 seconds, then stop");
        telemetry.update();
        waitForStart();

        if (isStopRequested()) {
            return;
        }

        try {
            setAllDrivePower(frontLeft, frontRight, backLeft, backRight, FORWARD_POWER);
            sleep(DRIVE_TIME_MILLISECONDS);
        } finally {
            setAllDrivePower(frontLeft, frontRight, backLeft, backRight, 0);
        }
    }

    /** Applies equal power because this test verifies straight-forward motor direction only. */
    private static void setAllDrivePower(DcMotor frontLeft, DcMotor frontRight,
            DcMotor backLeft, DcMotor backRight, double power) {
        frontLeft.setPower(power);
        frontRight.setPower(power);
        backLeft.setPower(power);
        backRight.setPower(power);
    }
}

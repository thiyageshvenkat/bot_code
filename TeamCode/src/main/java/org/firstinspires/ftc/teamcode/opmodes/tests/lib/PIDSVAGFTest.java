package org.firstinspires.ftc.teamcode.opmodes.tests.lib;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.util.controllers.PIDSVAGFController;
import org.firstinspires.ftc.teamcode.util.telemetry.AdvantageScopeHelper;
import org.firstinspires.ftc.teamcode.util.telemetry.TelemetryEx;
import org.firstinspires.ftc.teamcode.util.opMode.LinearOpModeEx;

/**
 * A simple test opmode for the PIDSVAGFController. This opmode allows you to manually control a
 * motor using the left stick of gamepad1, while also allowing you to adjust the D coefficient of
 * the PID controller using the dpad up and down buttons. It can be modified to adjust the other
 * coefficients as well. The motor's current position, velocity, and angle are displayed on the
 * telemetry.
 */
@Utility(name = "PIDSVAGF Test", description = "Test the functionality of the PIDSVAGFController.")
public class PIDSVAGFTest extends LinearOpModeEx {

    DcMotorEx motor;

    private static final double TICKS_PER_REV = 103.8; // 5203 GoBilda 3:7.1

    PIDSVAGFController controller = new PIDSVAGFController(
            0.001953125, 0.00, 5.64e-5, // PID coefficients
            0.040, 1e-4, 0.0, // SVA coefficients
            0.0, 0.0, // Feedforward and gravity compensation coefficients
            PIDSVAGFController.GravityCompensationMode.NONE // Gravity compensation mode
    );

    private double delta = 0.5;

    @Override
    public void run() {
        motor = hardwareMap.get(DcMotorEx.class, "motor");

        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        controller.setSetPoint(0.0);
        controller.setTolerance(1.0);

        TelemetryEx.getInstance()
                .addLine("TeleOp Initialized")
                .forceUpdate();

        waitForStart();

        TelemetryEx.getInstance().useAdvantageScopePath("PIDSVAGFTest");
        AdvantageScopeHelper.start();

        controller.reset();

        while (opModeIsActive()) {
            double power = gamepad1.left_stick_y;
            if (Math.abs(power) < 0.1) power = controller.calculateOutput(motor.getCurrentPosition());

            // binary search for the best D value
            // decimal search might be better
            if (gamepad1.dpadUpWasPressed()) {
                controller.setD(controller.getD() + delta);
                delta /= 2.0;
            } else if (gamepad1.dpadDownWasPressed()) {
                controller.setD(controller.getD() - delta);
                delta /= 2.0;
            }

            motor.setPower(power);

            TelemetryEx.getInstance()
                    .addData("Motor Power", motor.getPower())
                    .addData("Controller D", controller.getD())
                    .addData("Motor Position", motor.getCurrentPosition())
                    .addData("Motor Angle", motor.getCurrentPosition() * 360.0 / TICKS_PER_REV)
                    .addData("Motor Velocity (Ticks/s)", motor.getVelocity())
                    .addData("Motor Velocity (deg/s)", motor.getVelocity() * 360.0 / TICKS_PER_REV)

                    .requestUpdate();
        }
    }

}

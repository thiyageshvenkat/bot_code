package org.firstinspires.ftc.teamcode.opmodes.tests;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.opMode.LinearOpModeEx;
import org.firstinspires.ftc.teamcode.util.telemetry.TelemetryEx;

import java.util.ArrayList;
import java.util.List;

@Utility(name = "Robot Pre-Match Checklist", description = "Check the status of all hardware devices before a match.")
public class ChecklistOpMode extends LinearOpModeEx {

    // Store device names and objects sequentially
    private final List<String> deviceNames = new ArrayList<>();
    private final List<HardwareDevice> deviceObjects = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    public void run() {
        // 1. Dynamic Discovery Phase (Populate our checklist)
        for (String name : hardwareMap.getAllNames(HardwareDevice.class)) {
            HardwareDevice device = hardwareMap.get(HardwareDevice.class, name);
            String className = device.getClass().getSimpleName();

            if (name.contains("Lynx") || name.contains("Hub") ||
                    className.contains("Lynx") || className.contains("Hub")) continue;

            deviceNames.add(name);
            deviceObjects.add(device);
        }

        TelemetryEx.getInstance().addLine("=== CHECKLIST INITIALIZED ===")
                .addData("Total Devices Found", deviceNames.size())
                .addLine("Press START to begin testing.")
                .requestUpdate();

        waitForStart();

        if (deviceNames.isEmpty()) {
            TelemetryEx.getInstance().addLine("No devices found in configuration!")
                    .requestUpdate();
            while (opModeIsActive());
            return;
        }

        while (opModeIsActive()) {
            // 2. Handle Checklist Navigation (D-pad Up/Down)
            handleNavigation();

            // 3. Get currently selected device details
            String currentName = deviceNames.get(currentIndex);
            HardwareDevice currentDevice = deviceObjects.get(currentIndex);

            TelemetryEx.getInstance().addLine("--------------------------------")
                    .addLine("Controls: Left/Right Bumper to switch devices")
                    .space()
                    .addLine("=== PRE-MATCH CHECKLIST ===")
                    .addData("Device #", (currentIndex + 1) + " / " + deviceNames.size())
                    .addData("Selected Name", currentName)
                    .addData("Class Type", currentDevice.getClass().getSimpleName())
                    .addLine("--------------------------------");

            // 4. Interface Testing and Data Monitoring

            // Case A: Standard DC Motor
            if (currentDevice instanceof DcMotor) {
                DcMotor motor = (DcMotor) currentDevice;
                // Control power using Left Joystick Y (inverted for intuitive forward/back)
                double power = -gamepad1.left_stick_y;
                motor.setPower(power);

                TelemetryEx.getInstance().addLine("[TEST] Move LEFT JOYSTICK Y to spin motor")
                        .addData("Target Power", "%.2f", power)
                        .addData("Current Position", motor.getCurrentPosition());
            }

            // Case B: Continuous Rotation Servo
            else if (currentDevice instanceof CRServo) {
                CRServo crServo = (CRServo) currentDevice;
                // Control power using Left Joystick Y
                double power = -gamepad1.left_stick_y;
                crServo.setPower(power);

                TelemetryEx.getInstance().addLine("[TEST] Move LEFT JOYSTICK Y to spin CR Servo")
                        .addData("Target Power", "%.2f", power);
            }

            // Case C: Standard Position Servo
            else if (currentDevice instanceof Servo) {
                Servo servo = (Servo) currentDevice;
                // Map Left Trigger (0 to 1) directly to servo position
                double position = gamepad1.left_trigger;
                servo.setPosition(position);

                TelemetryEx.getInstance().addLine("[TEST] Press LEFT TRIGGER to sweep position (0 to 1)")
                        .addData("Target Position", "%.2f", position);
            }

            // Case D: Distance Sensor
            else if (currentDevice instanceof DistanceSensor) {
                DistanceSensor distanceSensor = (DistanceSensor) currentDevice;
                double reading = distanceSensor.getDistance(DistanceUnit.INCH);

                TelemetryEx.getInstance().addLine("[SENSOR DATA] Wave hand in front of sensor")
                        .addData("Distance", "%.2f inches", reading);
            }

            // Case E: Color Sensor
            else if (currentDevice instanceof ColorSensor) {
                ColorSensor colorSensor = (ColorSensor) currentDevice;

                TelemetryEx.getInstance().addLine("[SENSOR DATA] Place colored object near lens")
                        .addData("Red", colorSensor.red())
                        .addData("Green", colorSensor.green())
                        .addData("Blue", colorSensor.blue())
                        .addData("Alpha (Clear)", colorSensor.alpha());
            }

            // Case F: Digital Switch
            else if (currentDevice instanceof DigitalChannel) {
                DigitalChannel touchSensor = (DigitalChannel) currentDevice;

                TelemetryEx.getInstance().addLine("[SENSOR DATA] Press physical button/limit switch")
                        .addData("Is Active", touchSensor.getState());
            }

            // Fallback: Webcams, Hubs, or generic I2C drivers
            else {
                TelemetryEx.getInstance().addLine("[GENERIC DEVICE] No manual controls written.")
                        .addData("Connection Info", currentDevice.getConnectionInfo());
            }

            TelemetryEx.getInstance().requestUpdate();
        }

        // 5. Emergency Stop Safety: Shut off all actuators when exiting loop
        stopAllActuators();
    }

    private void handleNavigation() {
        // Right Bumper moves forward through list
        if (gamepad1.rightBumperWasPressed()) {
            currentIndex = (currentIndex + 1) % deviceNames.size();
            stopAllActuators(); // Turn off the old device before switching!
        }

        // Left Bumper moves backward through list
        if (gamepad1.leftBumperWasPressed()) {
            currentIndex = (currentIndex - 1 + deviceNames.size()) % deviceNames.size();
            stopAllActuators(); // Turn off the old device before switching!
        }
    }

    private void stopAllActuators() {
        for (HardwareDevice device : deviceObjects) {
            if (device instanceof DcMotor) {
                ((DcMotor) device).setPower(0);
            } else if (device instanceof CRServo) {
                ((CRServo) device).setPower(0);
            }
        }
    }
}

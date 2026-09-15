package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.seattlesolvers.solverslib.hardware.motors.Motor;

import org.firstinspires.ftc.teamcode.util.controllers.PIDSVAGFController;
import org.firstinspires.ftc.teamcode.util.telemetry.TelemetryEx;

import java.util.Locale;

/**
 * Utility OpMode for tuning PIDSVAGF position controllers.
 *
 * <p>IMPORTANT: This file is intentionally configured by editing the
 * HARDWARE / ENCODER CONFIG section below. There is no dependency on
 * DcMotor or DcMotorEx in the controller loop.</p>
 *
 * <p>Supported use cases:</p>
 * <ul>
 *     <li>One motor</li>
 *     <li>A pair of motors controlled with the same power</li>
 *     <li>Custom position / velocity readers (external encoders, turret angle, etc.)</li>
 *     <li>Two target modes: manual-to-zero and joystick-commanded target</li>
 *     <li>Decimal coefficient search with D-pad + bumper coefficient selection</li>
 *     <li>Experimental automatic kS finder</li>
 *     <li>Experimental full-power V/A identification test</li>
 * </ul>
 */
@TeleOp(name = "Utility - PIDSVAGF Tuner", group = "Utility")
public class PIDSVAGFTuner extends LinearOpMode {

    // =====================================================================
    // ======================== EDIT THIS SECTION ==========================
    // =====================================================================

    /**
     * Put one name here for a single motor, or two names for a paired motor.
     * The same controller output is sent to every motor in this array.
     */
    private static final String[] MOTOR_NAMES = {
            "yourMotor"
            // , "yourSecondMotor"
    };

    /**
     * Optional inversion per motor. Keep the array the same length as MOTOR_NAMES.
     * true means that motor's commanded power is inverted.
     */
    private static final boolean[] MOTOR_INVERTED = {
            false
            // , false
    };

    /**
     * Initial coefficients: P, I, D, S, V, A, G, F.
     * Replace these with the values you want to start from.
     */
    private static final double INITIAL_P = 0.0;
    private static final double INITIAL_I = 0.0;
    private static final double INITIAL_D = 0.0;
    private static final double INITIAL_S = 0.0;
    private static final double INITIAL_V = 0.0;
    private static final double INITIAL_A = 0.0;
    private static final double INITIAL_G = 0.0;
    private static final double INITIAL_F = 0.0;

    /**
     * Gravity compensation mode used by your PIDSVAGFController.
     */
    private static final PIDSVAGFController.GravityCompensationMode GRAVITY_MODE =
            PIDSVAGFController.GravityCompensationMode.NONE;

    /**
     * Only used if GRAVITY_MODE == COSINE.
     */
    private static final boolean USE_DEGREES_FOR_GRAVITY = true;

    /**
     * Initial position target for the target-command mode, in the units returned
     * by readPosition(). For a typical motor this will be encoder ticks.
     */
    private static final double INITIAL_TARGET_POSITION = 0.0;

    /**
     * Joystick sensitivity in target-command mode.
     * Units: position-units / second at full joystick deflection.
     */
    private static final double TARGET_POSITION_SENSITIVITY = 800.0;

    /**
     * Joystick deadband used for manual control / target changes.
     */
    private static final double JOYSTICK_DEADBAND = 0.08;

    /**
     * Decimal search starts here. D-pad up adds delta, D-pad down subtracts it.
     */
    private static final double INITIAL_DELTA = 0.1;

    /**
     * Prevents coefficients from accidentally going negative through the tuner.
     * Set false if you intentionally tune signed coefficients.
     */
    private static final boolean CLAMP_TUNED_COEFFICIENTS_TO_ZERO = true;

    /**
     * Position tolerance used for the controller's atSetPoint() state.
     */
    private static final double POSITION_TOLERANCE = 5.0;
    private static final double VELOCITY_TOLERANCE = 20.0;

    /**
     * Maximum motor command sent by this utility.
     */
    private static final double MAX_POWER = 1.0;

    // -------------------- Automatic kS finder ----------------------------
    private static final double KS_TEST_START_POWER = 0.0;
    private static final double KS_TEST_POWER_STEP = 0.02;
    private static final long KS_TEST_STEP_MS = 125;
    private static final double KS_MOTION_THRESHOLD = 30.0; // velocity units / sec
    private static final long KS_SETTLE_MS = 100;

    // -------------------- Full-power V/A test -----------------------------
    private static final double FULL_POWER_COMMAND = 1.0;
    private static final long FULL_POWER_MAX_DURATION_MS = 3000;
    private static final int FULL_POWER_MIN_SAMPLES = 30;
    private static final long FULL_POWER_MIN_HOLD_MS = 150;

    /**
     * During the full-power test, samples with very tiny dt are ignored.
     */
    private static final double MIN_SAMPLE_DT_SEC = 0.005;

    /**
     * Custom position reader. Replace this method to use an external encoder,
     * absolute sensor, turret angle, etc.
     *
     * <p>For a pair of motors, the default implementation returns the average
     * position. You can instead choose one motor or read any other sensor.</p>
     */
    private double readPosition(Motor[] motors) {
        double sum = 0.0;
        for (Motor motor : motors) {
            sum += motor.getCurrentPosition();
        }
        return sum / motors.length;
    }

    /**
     * Custom velocity reader. Replace this method when using a different encoder
     * or a differently calculated velocity.
     *
     * <p>SolversLib documents getCorrectedVelocity() as the corrected velocity
     * available from Motor; that is the default used here.</p>
     */
    private double readVelocity(Motor[] motors) {
        double sum = 0.0;
        for (Motor motor : motors) {
            sum += motor.getCorrectedVelocity();
        }
        return sum / motors.length;
    }

    /**
     * Apply the same controller output to every configured motor. Modify this
     * method if a pair needs different output transforms.
     */
    private void setMotorPower(Motor[] motors, double power) {
        for (int i = 0; i < motors.length; i++) {
            double actual = MOTOR_INVERTED[i] ? -power : power;
            motors[i].set(actual);
        }
    }

    // =====================================================================
    // ========================= INTERNAL STATE ============================
    // =====================================================================

    private enum TargetMode {
        MANUAL_TO_ZERO,
        TARGET_WITH_JOYSTICK
    }

    private enum Coefficient {
        P, I, D, S, V, A, G, F
    }

    private enum AutoTest {
        NONE,
        FIND_S,
        FULL_POWER_VA
    }

    private enum KsDirection {
        POSITIVE,
        NEGATIVE
    }

    private Motor[] motors;
    private PIDSVAGFController controller;
    private TelemetryEx telemetryEx;

    private TargetMode targetMode = TargetMode.MANUAL_TO_ZERO;
    private Coefficient selectedCoefficient = Coefficient.S;
    private AutoTest selectedAutoTest = AutoTest.NONE;

    private double delta = INITIAL_DELTA;
    private double targetPosition = INITIAL_TARGET_POSITION;
    private double targetVelocity = 0.0;
    private double targetAcceleration = 0.0;

    private double lastTargetPosition;
    private double lastTargetVelocity;

    private boolean controllerActive = true;
    private boolean lastControllerActive;
    private double lastControlOutput;

    private final ElapsedTime loopTimer = new ElapsedTime();
    private double loopDt;
    private double loopRateHz;

    // Rising-edge bookkeeping.
    private boolean lastDpadUp;
    private boolean lastDpadDown;
    private boolean lastDpadLeft;
    private boolean lastDpadRight;
    private boolean lastLeftBumper;
    private boolean lastRightBumper;
    private boolean lastX;
    private boolean lastY;
    private boolean lastA;
    private boolean lastB;
    private boolean lastStart;

    // ============================ kS test ================================
    private boolean ksTestRunning;
    private KsDirection ksDirection = KsDirection.POSITIVE;
    private double ksTestPower;
    private long ksPhaseStartMs;
    private double ksPositiveThreshold = Double.NaN;
    private double ksNegativeThreshold = Double.NaN;
    private boolean ksWaitingAfterMotion;

    // =========================== V/A test ================================
    private boolean fullPowerTestRunning;
    private long fullPowerTestStartMs;
    private double fullPowerLastTimeSec;
    private double fullPowerLastVelocity;
    private int fullPowerSampleCount;

    // Normal equations for least-squares fit:
    // y = kV * velocity + kA * acceleration
    // where y = appliedPower - kS * sign(velocity)
    private double sumVV;
    private double sumVA;
    private double sumAA;
    private double sumVY;
    private double sumAY;

    private double estimatedV = Double.NaN;
    private double estimatedA = Double.NaN;

    // =====================================================================
    // ============================= OpMode =================================
    // =====================================================================

    @Override
    public void runOpMode() throws InterruptedException {
        validateConfiguration();

        motors = createMotors();

        controller = new PIDSVAGFController(
                INITIAL_P, INITIAL_I, INITIAL_D,
                INITIAL_S, INITIAL_V, INITIAL_A,
                INITIAL_G, INITIAL_F,
                GRAVITY_MODE
        );

        if (USE_DEGREES_FOR_GRAVITY) {
            controller.setAngleUnit(org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES);
        } else {
            controller.setAngleUnit(org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS);
        }

        controller.setTolerance(POSITION_TOLERANCE, VELOCITY_TOLERANCE);
        controller.setSetPoint(targetPosition);
        controller.setSetVelocity(0.0);
        controller.setSetAcceleration(0.0);

        telemetryEx = TelemetryEx.getInstance();
        telemetryEx.init(telemetry, 20);
        telemetryEx.useAdvantageScopePath("PIDSVAGF Tuner");

        lastTargetPosition = targetPosition;
        lastTargetVelocity = 0.0;
        loopTimer.reset();

        setMotorPower(motors, 0.0);

        // Initialization telemetry.
        addTelemetry(readPosition(motors), readVelocity(motors), 0.0, 0.0);
        telemetryEx.forceUpdate();

        waitForStart();
        if (isStopRequested()) return;

        loopTimer.reset();

        while (opModeIsActive()) {
            long loopStartNs = System.nanoTime();
            loopDt = Math.max(0.0001, loopTimer.seconds());
            loopTimer.reset();
            loopRateHz = 1.0 / loopDt;

            handleUi();

            double position = readPosition(motors);
            double velocity = readVelocity(motors);

            if (ksTestRunning) {
                runKsAutotune(position, velocity);
            } else if (fullPowerTestRunning) {
                runFullPowerTest(position, velocity);
            } else {
                updateTargetFromJoystick();
                runNormalController(position);
            }

            addTelemetry(position, velocity, loopDt, (System.nanoTime() - loopStartNs) / 1e6);
            telemetryEx.requestUpdate();
        }

        setMotorPower(motors, 0.0);
    }

    // =====================================================================
    // ============================ CONFIG ==================================
    // =====================================================================

    private void validateConfiguration() {
        if (MOTOR_NAMES.length == 0) {
            throw new IllegalStateException("MOTOR_NAMES must contain at least one motor name.");
        }
        if (MOTOR_INVERTED.length != MOTOR_NAMES.length) {
            throw new IllegalStateException("MOTOR_INVERTED must have the same length as MOTOR_NAMES.");
        }
    }

    private Motor[] createMotors() {
        Motor[] result = new Motor[MOTOR_NAMES.length];
        for (int i = 0; i < MOTOR_NAMES.length; i++) {
            result[i] = new Motor(hardwareMap, MOTOR_NAMES[i]);
            result[i].setRunMode(Motor.RunMode.RawPower);
            result[i].setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
            result[i].setInverted(false); // per-motor inversion is handled by setMotorPower().
        }
        return result;
    }

    // =====================================================================
    // =========================== GAMEPAD UI ===============================
    // =====================================================================

    private void handleUi() {
        // D-pad: decimal coefficient search.
        if (pressed(gamepad1.dpad_up, lastDpadUp)) changeSelectedCoefficient(+delta);
        if (pressed(gamepad1.dpad_down, lastDpadDown)) changeSelectedCoefficient(-delta);
        if (pressed(gamepad1.dpad_left, lastDpadLeft)) delta *= 10.0;
        if (pressed(gamepad1.dpad_right, lastDpadRight)) delta /= 10.0;

        // Bumpers: move through PIDSVAGF coefficients.
        if (pressed(gamepad1.left_bumper, lastLeftBumper)) selectedCoefficient = previousCoefficient(selectedCoefficient);
        if (pressed(gamepad1.right_bumper, lastRightBumper)) selectedCoefficient = nextCoefficient(selectedCoefficient);

        // X: target mode.
        if (pressed(gamepad1.x, lastX)) {
            targetMode = targetMode == TargetMode.MANUAL_TO_ZERO
                    ? TargetMode.TARGET_WITH_JOYSTICK
                    : TargetMode.MANUAL_TO_ZERO;

            controller.reset();
            if (targetMode == TargetMode.MANUAL_TO_ZERO) {
                targetPosition = 0.0;
                targetVelocity = 0.0;
                targetAcceleration = 0.0;
            }
            controller.setSetPoint(targetPosition);
        }

        // Y: cycle automatic test menu.
        if (pressed(gamepad1.y, lastY)) {
            selectedAutoTest = nextAutoTest(selectedAutoTest);
        }

        // A: start selected automatic test / stop it if already running.
        if (pressed(gamepad1.a, lastA)) {
            if (ksTestRunning || fullPowerTestRunning) {
                stopAutomaticTest();
            } else if (selectedAutoTest == AutoTest.FIND_S) {
                startKsAutotune();
            } else if (selectedAutoTest == AutoTest.FULL_POWER_VA) {
                startFullPowerTest();
            }
        }

        // B: cancel an automatic test.
        if (pressed(gamepad1.b, lastB)) {
            stopAutomaticTest();
        }

        // Start: zero target and reset controller state.
        if (pressed(gamepad1.start, lastStart)) {
            targetPosition = 0.0;
            targetVelocity = 0.0;
            targetAcceleration = 0.0;
            controller.reset();
            controller.setSetPoint(targetPosition);
        }

        lastDpadUp = gamepad1.dpad_up;
        lastDpadDown = gamepad1.dpad_down;
        lastDpadLeft = gamepad1.dpad_left;
        lastDpadRight = gamepad1.dpad_right;
        lastLeftBumper = gamepad1.left_bumper;
        lastRightBumper = gamepad1.right_bumper;
        lastX = gamepad1.x;
        lastY = gamepad1.y;
        lastA = gamepad1.a;
        lastB = gamepad1.b;
        lastStart = gamepad1.start;
    }

    private boolean pressed(boolean current, boolean previous) {
        return current && !previous;
    }

    private Coefficient nextCoefficient(Coefficient c) {
        return Coefficient.values()[(c.ordinal() + 1) % Coefficient.values().length];
    }

    private Coefficient previousCoefficient(Coefficient c) {
        int next = c.ordinal() - 1;
        if (next < 0) next = Coefficient.values().length - 1;
        return Coefficient.values()[next];
    }

    private AutoTest nextAutoTest(AutoTest test) {
        AutoTest[] values = AutoTest.values();
        return values[(test.ordinal() + 1) % values.length];
    }

    // =====================================================================
    // ========================= NORMAL CONTROL ============================
    // =====================================================================

    private void updateTargetFromJoystick() {
        if (targetMode != TargetMode.TARGET_WITH_JOYSTICK) {
            targetPosition = 0.0;
            targetVelocity = 0.0;
            targetAcceleration = 0.0;
            return;
        }

        double stick = applyDeadband(-gamepad1.left_stick_y, JOYSTICK_DEADBAND);
        targetPosition += stick * TARGET_POSITION_SENSITIVITY * loopDt;

        // Calculate requested target velocity and target acceleration.
        double newTargetVelocity = stick * TARGET_POSITION_SENSITIVITY;
        double newTargetAcceleration = (newTargetVelocity - lastTargetVelocity) / loopDt;

        targetVelocity = newTargetVelocity;
        targetAcceleration = newTargetAcceleration;
        lastTargetVelocity = targetVelocity;
        lastTargetPosition = targetPosition;
    }

    private double applyDeadband(double value, double deadband) {
        if (Math.abs(value) <= deadband) return 0.0;
        double sign = Math.signum(value);
        return sign * (Math.abs(value) - deadband) / (1.0 - deadband);
    }

    private void runNormalController(double position) {
        double manualPower = -gamepad1.left_stick_y;
        boolean manualOverride = targetMode == TargetMode.MANUAL_TO_ZERO
                && Math.abs(manualPower) > JOYSTICK_DEADBAND;

        controllerActive = !manualOverride;

        if (controllerActive && !lastControllerActive) {
            controller.reset();
        }

        if (!controllerActive) {
            lastControlOutput = clip(manualPower, -MAX_POWER, MAX_POWER);
            setMotorPower(motors, lastControlOutput);
            controller.setSetPoint(0.0);
            controller.setSetVelocity(0.0);
            controller.setSetAcceleration(0.0);
        } else {
            controller.setSetPoint(targetPosition);
            controller.setSetVelocity(targetVelocity);
            controller.setSetAcceleration(targetAcceleration);
            double output = controller.calculate(position);
            lastControlOutput = clip(output, -MAX_POWER, MAX_POWER);
            setMotorPower(motors, lastControlOutput);
        }

        lastControllerActive = controllerActive;
    }

    // =====================================================================
    // =========================== COEFFICIENTS =============================
    // =====================================================================

    private void changeSelectedCoefficient(double amount) {
        double value = getSelectedCoefficient();
        value += amount;
        if (CLAMP_TUNED_COEFFICIENTS_TO_ZERO) value = Math.max(0.0, value);
        setSelectedCoefficient(value);

        // A gain change can make the old integrator/derivative state misleading.
        controller.reset();
    }

    private double getSelectedCoefficient() {
        switch (selectedCoefficient) {
            case P: return controller.getP();
            case I: return controller.getI();
            case D: return controller.getD();
            case S: return controller.getS();
            case V: return controller.getV();
            case A: return controller.getA();
            case G: return controller.getG();
            case F: return controller.getF();
            default: throw new IllegalStateException("Unknown coefficient");
        }
    }

    private void setSelectedCoefficient(double value) {
        switch (selectedCoefficient) {
            case P: controller.setP(value); break;
            case I: controller.setI(value); break;
            case D: controller.setD(value); break;
            case S: controller.setS(value); break;
            case V: controller.setV(value); break;
            case A: controller.setA(value); break;
            case G: controller.setG(value); break;
            case F: controller.setF(value); break;
            default: throw new IllegalStateException("Unknown coefficient");
        }
    }

    // =====================================================================
    // ============================== kS ====================================
    // =====================================================================

    private void startKsAutotune() {
        stopAutomaticTest();
        ksTestRunning = true;
        controllerActive = false;
        ksDirection = KsDirection.POSITIVE;
        ksTestPower = KS_TEST_START_POWER;
        ksPhaseStartMs = System.currentTimeMillis();
        ksPositiveThreshold = Double.NaN;
        ksNegativeThreshold = Double.NaN;
        ksWaitingAfterMotion = false;
        setMotorPower(motors, 0.0);
    }

    private void runKsAutotune(double position, double velocity) {
        long now = System.currentTimeMillis();

        if (ksWaitingAfterMotion) {
            setMotorPower(motors, 0.0);
            if (now - ksPhaseStartMs >= KS_SETTLE_MS) {
                if (ksDirection == KsDirection.POSITIVE) {
                    ksDirection = KsDirection.NEGATIVE;
                    ksTestPower = KS_TEST_START_POWER;
                } else {
                    double result = averageFinite(ksPositiveThreshold, ksNegativeThreshold);
                    if (!Double.isNaN(result)) {
                        controller.setS(result);
                    }
                    stopAutomaticTest();
                }
                ksWaitingAfterMotion = false;
                ksPhaseStartMs = now;
            }
            return;
        }

        double direction = ksDirection == KsDirection.POSITIVE ? 1.0 : -1.0;
        double command = direction * ksTestPower;
        setMotorPower(motors, command);

        if (Math.abs(velocity) >= KS_MOTION_THRESHOLD) {
            if (ksDirection == KsDirection.POSITIVE) ksPositiveThreshold = ksTestPower;
            else ksNegativeThreshold = ksTestPower;

            ksWaitingAfterMotion = true;
            ksPhaseStartMs = now;
            return;
        }

        if (now - ksPhaseStartMs >= KS_TEST_STEP_MS) {
            ksTestPower += KS_TEST_POWER_STEP;
            ksPhaseStartMs = now;

            if (ksTestPower > MAX_POWER) {
                // Could not find a threshold on this side.
                if (ksDirection == KsDirection.POSITIVE) {
                    ksPositiveThreshold = Double.NaN;
                    ksDirection = KsDirection.NEGATIVE;
                    ksTestPower = KS_TEST_START_POWER;
                } else {
                    ksNegativeThreshold = Double.NaN;
                    stopAutomaticTest();
                }
            }
        }
    }

    // =====================================================================
    // ============================ V / A ==================================
    // =====================================================================

    private void startFullPowerTest() {
        stopAutomaticTest();

        fullPowerTestRunning = true;
        controllerActive = false;
        fullPowerTestStartMs = System.currentTimeMillis();
        fullPowerLastTimeSec = Double.NaN;
        fullPowerLastVelocity = 0.0;
        fullPowerSampleCount = 0;

        sumVV = 0.0;
        sumVA = 0.0;
        sumAA = 0.0;
        sumVY = 0.0;
        sumAY = 0.0;
        estimatedV = Double.NaN;
        estimatedA = Double.NaN;

        setMotorPower(motors, 0.0);
    }

    private void runFullPowerTest(double position, double velocity) {
        long nowMs = System.currentTimeMillis();
        double nowSec = (nowMs - fullPowerTestStartMs) / 1000.0;
        setMotorPower(motors, FULL_POWER_COMMAND);

        if (!Double.isNaN(fullPowerLastTimeSec)) {
            double dt = nowSec - fullPowerLastTimeSec;
            if (dt >= MIN_SAMPLE_DT_SEC) {
                double acceleration = (velocity - fullPowerLastVelocity) / dt;
                double y = FULL_POWER_COMMAND
                        - controller.getS() * Math.signum(velocity);

                // Accumulate normal equations for y = kV*v + kA*a.
                sumVV += velocity * velocity;
                sumVA += velocity * acceleration;
                sumAA += acceleration * acceleration;
                sumVY += velocity * y;
                sumAY += acceleration * y;
                fullPowerSampleCount++;

                fullPowerLastVelocity = velocity;
                fullPowerLastTimeSec = nowSec;
            }
        } else {
            fullPowerLastTimeSec = nowSec;
            fullPowerLastVelocity = velocity;
        }

        boolean releasedAfterMinimumHold = !gamepad1.a
                && nowMs - fullPowerTestStartMs >= FULL_POWER_MIN_HOLD_MS;
        boolean timedOut = nowMs - fullPowerTestStartMs >= FULL_POWER_MAX_DURATION_MS;

        if (releasedAfterMinimumHold || timedOut) {
            finishFullPowerTest();
        }
    }

    private void finishFullPowerTest() {
        setMotorPower(motors, 0.0);

        // Solve [VV VA; VA AA] [kV; kA] = [VY; AY].
        double determinant = sumVV * sumAA - sumVA * sumVA;
        if (Math.abs(determinant) > 1e-9 && fullPowerSampleCount >= FULL_POWER_MIN_SAMPLES) {
            estimatedV = (sumVY * sumAA - sumAY * sumVA) / determinant;
            estimatedA = (sumVV * sumAY - sumVA * sumVY) / determinant;

            if (Double.isFinite(estimatedV)) controller.setV(Math.max(0.0, estimatedV));
            if (Double.isFinite(estimatedA)) controller.setA(Math.max(0.0, estimatedA));
        }

        fullPowerTestRunning = false;
        controllerActive = true;
        controller.reset();
    }

    // =====================================================================
    // =========================== TELEMETRY ================================
    // =====================================================================

    private void addTelemetry(double position, double velocity, double dt, double loopExecutionMs) {
        String coefficientLine = coefficientLine();
        String testLine = automaticTestLine();

        telemetryEx.useAdvantageScopePath("PIDSVAGF Tuner");

        telemetryEx.addLine("PIDSVAGF TUNER");
        telemetryEx.addLine("Tune order: G -> S -> V -> A -> P -> I -> D   (F normally left at 0)");
        telemetryEx.addLine(coefficientLine);
        telemetryEx.addLine(testLine);
        telemetryEx.addLine("Controls: DPAD=adjust/delta, bumpers=coefficient, X=target mode, Y=auto test, A=start/stop (hold A for V/A), B=cancel, START=zero");

        telemetryEx.addData("Motor Count", motors == null ? 0 : motors.length);
        telemetryEx.addData("Target Mode", targetMode);
        telemetryEx.addData("Controller Active", controllerActive);
        telemetryEx.addData("Position", "%.3f", position);
        telemetryEx.addData("Target Position", "%.3f", targetPosition);
        telemetryEx.addData("Position Error", "%.3f", targetPosition - position);
        telemetryEx.addData("Velocity", "%.3f", velocity);
        telemetryEx.addData("Target Velocity", "%.3f", targetVelocity);
        telemetryEx.addData("Target Acceleration", "%.3f", targetAcceleration);
        telemetryEx.addData("Control Output", "%.4f", lastControlOutput);
        telemetryEx.addData("Delta", "%.6g", delta);
        telemetryEx.space();

        telemetryEx.addData("kP", "%.7g", controller.getP());
        telemetryEx.addData("kI", "%.7g", controller.getI());
        telemetryEx.addData("kD", "%.7g", controller.getD());
        telemetryEx.addData("kS", "%.7g", controller.getS());
        telemetryEx.addData("kV", "%.7g", controller.getV());
        telemetryEx.addData("kA", "%.7g", controller.getA());
        telemetryEx.addData("kG", "%.7g", controller.getG());
        telemetryEx.addData("kF", "%.7g", controller.getF());
        telemetryEx.addData("At Setpoint", controller.atSetPoint());
        telemetryEx.addData("Loop Rate Hz", "%.1f", loopRateHz);
        telemetryEx.addData("Loop dt ms", "%.3f", dt * 1000.0);
        telemetryEx.addData("Loop exec ms", "%.3f", loopExecutionMs);

        // AdvantageScope-specific useful channels.
        telemetryEx.addData("State/Position", position);
        telemetryEx.addData("State/Velocity", velocity);
        telemetryEx.addData("State/TargetPosition", targetPosition);
        telemetryEx.addData("State/TargetVelocity", targetVelocity);
        telemetryEx.addData("State/TargetAcceleration", targetAcceleration);
        telemetryEx.addData("State/ControllerActive", controllerActive);
        telemetryEx.addData("State/Output", lastControlOutput);
        telemetryEx.addData("Gains/P", controller.getP());
        telemetryEx.addData("Gains/I", controller.getI());
        telemetryEx.addData("Gains/D", controller.getD());
        telemetryEx.addData("Gains/S", controller.getS());
        telemetryEx.addData("Gains/V", controller.getV());
        telemetryEx.addData("Gains/A", controller.getA());
        telemetryEx.addData("Gains/G", controller.getG());
        telemetryEx.addData("Gains/F", controller.getF());
    }

    private String coefficientLine() {
        return "Selected coefficient: " + selectedCoefficient.name()
                + "   PIDSVAGF: " + highlightedCoefficientLine();
    }

    private String highlightedCoefficientLine() {
        StringBuilder b = new StringBuilder();
        for (Coefficient c : Coefficient.values()) {
            if (c == selectedCoefficient) b.append("^");
            else b.append(" ");
            b.append(c.name());
        }
        return b.toString();
    }

    private String automaticTestLine() {
        if (ksTestRunning) {
            return String.format(Locale.US,
                    "AUTO: FIND S   direction=%s power=%.3f   +threshold=%.3f  -threshold=%.3f",
                    ksDirection, ksTestPower, ksPositiveThreshold, ksNegativeThreshold);
        }
        if (fullPowerTestRunning) {
            return String.format(Locale.US,
                    "AUTO: FULL POWER V/A   samples=%d   elapsed=%.2fs   estimatedV=%s estimatedA=%s",
                    fullPowerSampleCount,
                    (System.currentTimeMillis() - fullPowerTestStartMs) / 1000.0,
                    finiteString(estimatedV), finiteString(estimatedA));
        }
        return "AUTO TEST MENU: " + selectedAutoTest + "   (Y cycles, A starts, B cancels)";
    }

    private String finiteString(double value) {
        return Double.isFinite(value) ? String.format(Locale.US, "%.6g", value) : "---";
    }

    // =====================================================================
    // ============================== HELPERS ===============================
    // =====================================================================

    private void stopAutomaticTest() {
        ksTestRunning = false;
        fullPowerTestRunning = false;
        setMotorPower(motors, 0.0);
        controllerActive = true;
        controller.reset();
    }

    private double clip(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double averageFinite(double a, double b) {
        boolean aFinite = Double.isFinite(a);
        boolean bFinite = Double.isFinite(b);
        if (aFinite && bFinite) return (a + b) / 2.0;
        if (aFinite) return a;
        if (bFinite) return b;
        return Double.NaN;
    }
}

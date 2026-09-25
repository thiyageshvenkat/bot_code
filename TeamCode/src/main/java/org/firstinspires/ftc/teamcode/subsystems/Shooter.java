package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.control.ShotModel;

/**
 * Controls the two motors coupled to one flywheel, the feeder motor, and the adjustable hood.
 * Encoder feedback can prove that both flywheel motors reached speed. Because there is currently
 * no sensor at the feeder, completing a feed cycle only means that its timed motor pulse ended;
 * the CAD must reliably place the next pollen in front of the feeder.
 */
public final class Shooter extends SubsystemBase {
    // Normal progression is STOPPED -> SPINNING -> READY -> FEEDING -> SPINNING.
    public enum State {
        STOPPED,
        SPINNING,
        READY,
        FEEDING
    }

    private final DcMotorEx leftFlywheel;
    private final DcMotorEx rightFlywheel;
    private final DcMotorEx feeder;
    private final Servo hood;

    // While spinning, this measures uninterrupted time at the requested speed. While feeding, it
    // measures the feeder pulse. A single timer is safe because those states cannot occur together.
    private final ElapsedTime stateTimer = new ElapsedTime();

    private State state = State.STOPPED;
    // The FTC motor controller accepts encoder ticks per second, even though configuration and
    // telemetry use RPM because RPM is easier for the team to reason about while tuning.
    private double targetTicksPerSecond;
    // This is not sensor confirmation of a launched pollen; it records only a completed feed pulse.
    private boolean feedPulseCompleted;

    /**
     * Connects this subsystem to the configured REV devices when Superstructure is created during
     * OpMode initialization. A missing or incorrectly named device fails here, before the match
     * starts. Motor directions are chosen so the same positive velocity command should make both
     * motors assist the shared flywheel; that physical direction still must be verified safely.
     */
    public Shooter(HardwareMap hardwareMap) {
        leftFlywheel = hardwareMap.get(DcMotorEx.class, RobotConfig.Shooter.LEFT_FLYWHEEL);
        rightFlywheel = hardwareMap.get(DcMotorEx.class, RobotConfig.Shooter.RIGHT_FLYWHEEL);
        feeder = hardwareMap.get(DcMotorEx.class, RobotConfig.Shooter.FEEDER);
        hood = hardwareMap.get(Servo.class, RobotConfig.Shooter.HOOD);

        // Flywheel encoders provide the velocity feedback used to qualify the launcher as READY.
        configureFlywheel(leftFlywheel, DcMotorSimple.Direction.FORWARD);
        configureFlywheel(rightFlywheel,
                directionFromReversedSetting(RobotConfig.Shooter.RIGHT_FLYWHEEL_REVERSED));

        // There is no feedback from the feeder: the code applies a fixed power for a tuned amount
        // of time, then assumes the CAD moved one pollen into the flywheel.
        feeder.setDirection(directionFromReversedSetting(RobotConfig.Shooter.FEEDER_REVERSED));
        feeder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initialization must leave every actuator off until TeleOp or autonomous requests a shot.
        stop();
    }

    /** Applies a calculated RPM and hood position, then begins or maintains flywheel spin-up. */
    public void prepare(ShotModel solution) {
        if (solution == null) {
            return;
        }

        // Never send a negative launch speed, and never send the servo outside its valid range.
        targetTicksPerSecond = rpmToTicksPerSecond(Math.max(0.0, solution.shooterTargetRpm));
        hood.setPosition(Range.clip(solution.hoodPosition, 0.0, 1.0));

        // Both motors drive the same physical flywheel and therefore receive the same speed target.
        leftFlywheel.setVelocity(targetTicksPerSecond);
        rightFlywheel.setVelocity(targetTicksPerSecond);
        if (state == State.STOPPED) {
            state = State.SPINNING;
            stateTimer.reset();
        }
    }

    /**
     * Starts one timed feeder pulse only after both flywheel motors have qualified as ready.
     *
     * @return true when the request started a pulse; false when the shooter was not ready
     */
    public boolean requestFeed() {
        if (state != State.READY) {
            return false;
        }
        state = State.FEEDING;
        stateTimer.reset();
        feeder.setPower(RobotConfig.Shooter.FEED_POWER);
        return true;
    }

    /** Immediately makes the launcher safe and returns the hood to its stowed position. */
    public void stop() {
        leftFlywheel.setPower(0);
        rightFlywheel.setPower(0);
        feeder.setPower(0);
        hood.setPosition(RobotConfig.Shooter.HOOD_STOW);
        targetTicksPerSecond = 0.0;
        state = State.STOPPED;
    }

    public State getState() {
        return state;
    }

    public double getTargetRpm() {
        return ticksPerSecondToRpm(targetTicksPerSecond);
    }

    public double getLeftSpeedRpm() {
        return ticksPerSecondToRpm(leftFlywheel.getVelocity());
    }

    public double getRightSpeedRpm() {
        return ticksPerSecondToRpm(rightFlywheel.getVelocity());
    }

    /**
     * Reports each completed feeder pulse once so inventory can remove one assumed pollen.
     * This does not confirm that a pollen physically moved through the launcher.
     */
    public boolean consumeFeedPulseCompleted() {
        boolean completed = feedPulseCompleted;
        feedPulseCompleted = false;
        return completed;
    }

    /**
     * Rechecks the shooter during every robot-control loop after another method starts it.
     * While SPINNING, it reads both motor encoders and changes to READY only after both motors have
     * remained near the target RPM. While READY, it returns to SPINNING if either motor slows down.
     * While FEEDING, it stops the feeder when its timed pulse ends and reports that pulse so the
     * software inventory can remove one assumed pollen. It never starts a shot by itself.
     * Superstructure must call this repeatedly in both TeleOp and autonomous.
     */
    @Override
    public void periodic() {
        // Nothing is being timed or monitored until prepare() starts the flywheel.
        if (state == State.STOPPED) {
            return;
        }

        boolean bothMotorsAtSpeed = bothFlywheelMotorsAreAtTargetSpeed();

        if (state == State.SPINNING) {
            // READY requires continuous time within tolerance, not one lucky encoder reading.
            if (!bothMotorsAtSpeed) {
                stateTimer.reset();
            } else if (stateTimer.seconds() >= RobotConfig.Shooter.READY_HOLD_SECONDS) {
                state = State.READY;
            }
        } else if (state == State.READY && !bothMotorsAtSpeed) {
            // Revoke readiness if either motor slows before the feeder is requested.
            state = State.SPINNING;
            stateTimer.reset();
        } else if (state == State.FEEDING
                && stateTimer.seconds() >= RobotConfig.Shooter.FEED_SECONDS) {
            // Once feeding starts, finish the tuned pulse even if flywheel RPM falls. Interrupting
            // it could leave pollen partly engaged. The next shot must regain READY afterward.
            // With no feeder sensor, elapsed time is the only available completion signal.
            feeder.setPower(0);
            feedPulseCompleted = true;
            state = State.SPINNING;
            stateTimer.reset();
        }
    }

    private boolean bothFlywheelMotorsAreAtTargetSpeed() {
        double allowedError = rpmToTicksPerSecond(
                RobotConfig.Shooter.SHOOTER_MAX_READY_ERROR_RPM);
        double leftError = Math.abs(leftFlywheel.getVelocity() - targetTicksPerSecond);
        double rightError = Math.abs(rightFlywheel.getVelocity() - targetTicksPerSecond);

        // Requiring both protects against feeding with a disconnected, stalled, or slow motor.
        return leftError <= allowedError && rightError <= allowedError;
    }

    private static void configureFlywheel(DcMotorEx motor, DcMotorSimple.Direction direction) {
        motor.setPower(0);
        motor.setDirection(direction);
        // RUN_USING_ENCODER lets setVelocity use the motor controller's closed-loop regulation.
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // The high-speed flywheel should coast down instead of electrically braking to a stop.
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    private static DcMotorSimple.Direction directionFromReversedSetting(boolean reversed) {
        if (reversed) {
            return DcMotorSimple.Direction.REVERSE;
        }
        return DcMotorSimple.Direction.FORWARD;
    }

    private static double rpmToTicksPerSecond(double rpm) {
        return rpm * RobotConfig.Shooter.SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION / 60.0;
    }

    private static double ticksPerSecondToRpm(double ticksPerSecond) {
        return ticksPerSecond * 60.0
                / RobotConfig.Shooter.SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION;
    }
}

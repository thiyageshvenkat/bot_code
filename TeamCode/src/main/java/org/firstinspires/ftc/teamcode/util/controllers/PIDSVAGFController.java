package org.firstinspires.ftc.teamcode.util.controllers;

import com.seattlesolvers.solverslib.controller.PIDFController;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class PIDSVAGFController extends PIDFController {

    protected double kg;

    protected double setVelocity, setAcceleration;

    public enum GravityCompensationMode {
        NONE,
        CONSTANT,
        COSINE
    }
    protected GravityCompensationMode gravityMode;

    protected AngleUnit angleUnit = AngleUnit.DEGREES;

    private final SimpleMotorFeedforward feedforwardController;

    public PIDSVAGFController(double kp, double ki, double kd, double ks, double kv, double ka, double kg, double kf, GravityCompensationMode gravityMode) {
        super(kp, ki, kd, kf);
        feedforwardController = new SimpleMotorFeedforward(ks, kv, ka);
        this.kg = kg;
        this.gravityMode = gravityMode;
    }

    public PIDSVAGFController(PIDSVAGFCoefficients coefficients, GravityCompensationMode gravityMode) {
        this(
                coefficients.p,
                coefficients.i,
                coefficients.d,
                coefficients.s,
                coefficients.v,
                coefficients.a,
                coefficients.g,
                coefficients.f,
                gravityMode
        );
    }

    /**
     * Only relevant if this controller {@code gravityMode} is set to {@link GravityCompensationMode#COSINE}. Sets the angle unit for the cosine function used in gravity compensation.
     * @param angleUnit The angle unit to use for the cosine function in gravity compensation.
     */
    public void setAngleUnit(AngleUnit angleUnit) {
        this.angleUnit = angleUnit;
    }

    /**
     * Calculates and returns the output of the PIDSVAGFController based on the current measurement
     * of the process variable, the setpoint, and the feedforward and gravity compensation terms.
     * @param current The current measurement of the process variable.
     * @return the output of the PIDSVAGFController, which is the sum of the PID output, feedforward term, and gravity compensation term.
     */
    @Override
    public double calculateOutput(double current) {
        double pid = super.calculateOutput(current);

        double gravityFeedforward;
        if (gravityMode == GravityCompensationMode.NONE) {
            gravityFeedforward = 0.0;
        } else if (gravityMode == GravityCompensationMode.CONSTANT) {
            gravityFeedforward = kg;
        } else if (gravityMode == GravityCompensationMode.COSINE) {
            double radians = angleUnit == AngleUnit.DEGREES ? Math.toRadians(current) : current;
            gravityFeedforward = kg * Math.cos(Math.toRadians(radians));
        } else {
            throw new IllegalStateException("Unknown gravity compensation mode: " + gravityMode);
        }

        if (atSetPoint()) {
            return gravityFeedforward;
        }

        double feedforward;
        if (setVelocity == 0.0 && setAcceleration == 0.0) {
            feedforward = feedforwardController.ks * Math.signum(setPoint - current);
        } else {
            feedforward = feedforwardController.calculate(setVelocity, setAcceleration);
        }

        return pid + feedforward + gravityFeedforward;
    }

    public void setSetVelocity(double setVelocity) {
        this.setVelocity = setVelocity;
    }

    public void setSetAcceleration(double setAcceleration) {
        this.setAcceleration = setAcceleration;
    }

    @Override
    public void setP(double p) {
        super.setP(p);
    }

    @Override
    public void setI(double i) {
        super.setI(i);
    }

    public void setD(double d) {
        super.setD(d);
    }

    public void setS(double s) {
        feedforwardController.ks = s;
    }

    public void setV(double v) {
        feedforwardController.kv = v;
    }

    public void setA(double a) {
        feedforwardController.ka = a;
    }

    public void setG(double g) {
        this.kg = g;
    }

    public void setF(double f) {
        super.setF(f);
    }

    public void setCoefficients(PIDSVAGFCoefficients coefficients) {
        setP(coefficients.p);
        setI(coefficients.i);
        setD(coefficients.d);
        setS(coefficients.s);
        setV(coefficients.v);
        setA(coefficients.a);
        setG(coefficients.g);
        setF(coefficients.f);
    }

    public void setCoefficients(double[] coefficients) {
        assert coefficients.length == 8 : "Coefficients array must have length 8 (P, I, D, S, V, A, G, F)";

        setP(coefficients[0]);
        setI(coefficients[1]);
        setD(coefficients[2]);
        setS(coefficients[3]);
        setV(coefficients[4]);
        setA(coefficients[5]);
        setG(coefficients[6]);
        setF(coefficients[7]);
    }

    public double getS() {
        return feedforwardController.ks;
    }

    public double getV() {
        return feedforwardController.kv;
    }

    public double getA() {
        return feedforwardController.ka;
    }

    public double getG() {
        return kg;
    }

    public PIDSVAGFCoefficients getCoefficientsAsObject() {
        return new PIDSVAGFCoefficients(getP(), getI(), getD(), getS(), getV(), getA(), getG(), getF());
    }

    public double[] getCoefficientsAsArray() {
        return new double[]{getP(), getI(), getD(), getS(), getV(), getA(), getG(), getF()};
    }
}

/**
 * The exact same as {@link com.seattlesolvers.solverslib.controller.wpilibcontroller.SimpleMotorFeedforward} but the coefficients are not final.
 */
class SimpleMotorFeedforward {
    public double ks;
    public double kv;
    public double ka;

    /**
     * Creates a new SimpleMotorFeedforward with the specified gains.  Units of the gain values
     * will dictate units of the computed feedforward.
     *
     * @param ks The static gain.
     * @param kv The velocity gain.
     * @param ka The acceleration gain.
     */
    public SimpleMotorFeedforward(double ks, double kv, double ka) {
        this.ks = ks;
        this.kv = kv;
        this.ka = ka;
    }

    /**
     * Creates a new SimpleMotorFeedforward with the specified gains.  Acceleration gain is
     * defaulted to zero.  Units of the gain values will dictate units of the computed feedforward.
     *
     * @param ks The static gain.
     * @param kv The velocity gain.
     */
    public SimpleMotorFeedforward(double ks, double kv) {
        this(ks, kv, 0);
    }

    /**
     * Calculates the feedforward from the gains and setpoints.
     *
     * @param velocity     The velocity setpoint.
     * @param acceleration The acceleration setpoint.
     * @return The computed feedforward.
     */
    public double calculate(double velocity, double acceleration) {
        return ks * Math.signum(velocity) + kv * velocity + ka * acceleration;
    }

    // Rearranging the main equation from the calculate() method yields the
    // formulas for the methods below:

    /**
     * Calculates the feedforward from the gains and velocity setpoint (acceleration is assumed to
     * be zero).
     *
     * @param velocity The velocity setpoint.
     * @return The computed feedforward.
     */
    public double calculate(double velocity) {
        return calculate(velocity, 0);
    }

    /**
     * Calculates the maximum achievable velocity given a maximum voltage supply
     * and an acceleration.  Useful for ensuring that velocity and
     * acceleration constraints for a trapezoidal profile are simultaneously
     * achievable - enter the acceleration constraint, and this will give you
     * a simultaneously-achievable velocity constraint.
     *
     * @param maxVoltage   The maximum voltage that can be supplied to the motor.
     * @param acceleration The acceleration of the motor.
     * @return The maximum possible velocity at the given acceleration.
     */
    public double maxAchievableVelocity(double maxVoltage, double acceleration) {
        // Assume max velocity is positive
        return (maxVoltage - ks - acceleration * ka) / kv;
    }

    /**
     * Calculates the minimum achievable velocity given a maximum voltage supply
     * and an acceleration.  Useful for ensuring that velocity and
     * acceleration constraints for a trapezoidal profile are simultaneously
     * achievable - enter the acceleration constraint, and this will give you
     * a simultaneously-achievable velocity constraint.
     *
     * @param maxVoltage   The maximum voltage that can be supplied to the motor.
     * @param acceleration The acceleration of the motor.
     * @return The minimum possible velocity at the given acceleration.
     */
    public double minAchievableVelocity(double maxVoltage, double acceleration) {
        // Assume min velocity is negative, ks flips sign
        return (-maxVoltage + ks - acceleration * ka) / kv;
    }

    /**
     * Calculates the maximum achievable acceleration given a maximum voltage
     * supply and a velocity. Useful for ensuring that velocity and
     * acceleration constraints for a trapezoidal profile are simultaneously
     * achievable - enter the velocity constraint, and this will give you
     * a simultaneously-achievable acceleration constraint.
     *
     * @param maxVoltage The maximum voltage that can be supplied to the motor.
     * @param velocity   The velocity of the motor.
     * @return The maximum possible acceleration at the given velocity.
     */
    public double maxAchievableAcceleration(double maxVoltage, double velocity) {
        return (maxVoltage - ks * Math.signum(velocity) - velocity * kv) / ka;
    }

    /**
     * Calculates the maximum achievable acceleration given a maximum voltage
     * supply and a velocity. Useful for ensuring that velocity and
     * acceleration constraints for a trapezoidal profile are simultaneously
     * achievable - enter the velocity constraint, and this will give you
     * a simultaneously-achievable acceleration constraint.
     *
     * @param maxVoltage The maximum voltage that can be supplied to the motor.
     * @param velocity   The velocity of the motor.
     * @return The minimum possible acceleration at the given velocity.
     */
    public double minAchievableAcceleration(double maxVoltage, double velocity) {
        return maxAchievableAcceleration(-maxVoltage, velocity);
    }
}
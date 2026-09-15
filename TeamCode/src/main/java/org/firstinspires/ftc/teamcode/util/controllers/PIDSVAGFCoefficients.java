package org.firstinspires.ftc.teamcode.util.controllers;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.robotcore.internal.system.Misc;

public class PIDSVAGFCoefficients extends PIDFCoefficients {

    public double s;
    public double v;
    public double a;
    public double g;

    /**
     * Creates a new instance of PIDSVAGFCoefficients with the specified coefficients.
     * @param p the proportional coefficient
     * @param i the integral coefficient
     * @param d the derivative coefficient
     * @param s the static friction coefficient - the minimum amount of power needed to overcome static friction
     * @param v the velocity coefficient - the amount of power needed to maintain a certain velocity
     * @param a the acceleration coefficient - the amount of power needed to maintain a certain acceleration
     * @param g the gravity coefficient - the amount of power needed to overcome gravity
     * @param f the feedforward coefficient - the amount of power needed to maintain a certain velocity
     */
    public PIDSVAGFCoefficients(double p, double i, double d, double s, double v, double a, double g, double f) {
        super(p, i, d, f);
        this.s = s;
        this.v = v;
        this.a = a;
        this.g = g;
    }

    public PIDSVAGFCoefficients(PIDFCoefficients other, double s, double v, double a, double g) {
        super(other.p, other.i, other.d, other.f);
        this.s = s;
        this.v = v;
        this.a = a;
        this.g = g;
    }

    public PIDSVAGFCoefficients(PIDSVAGFCoefficients other) {
        super(other.p, other.i, other.d, other.f);
        this.s = other.s;
        this.v = other.v;
        this.a = other.a;
        this.g = other.g;
    }


    @NonNull
    @Override
    public String toString() {
        return Misc.formatForUser("%s(p=%f i=%f d=%f s=%f v=%f a=%f g=%f f=%f alg=%s)", getClass().getSimpleName(), p, i, d, s, v, a, g, f, algorithm);
    }

}

package com.tdunning;

import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Simulates a freezer.  This is done by having a micro-simulator that emulates
 * the physics of a freezer based on physical state variables (current temperature) and
 * external controlling variables (power available, door open, door slightly open).
 *
 * The controlling variables are simulated by mixing several discrete event
 * machines where each event is selected according to a simple Markov model. These
 * state machines represent external power and users of the freezer.
 *
 */
public class Freezer {

    private static final double DT = 0.25;

    public static void main(String[] args) {
        Sim s = new Sim();
        Door door = new Door(s);
        Power power = new Power(s);
        TemperatureModel model = new TemperatureModel();
        model.setTemp(TemperatureModel.EXTERNAL_TEMP);

        double t0 = new GregorianCalendar(2015, 6, 1).getTimeInMillis() / 1000.0;

        double time = t0;
        double step = t0 + DT;
        while (time < t0 + TimeUnit.SECONDS.convert(100, TimeUnit.DAYS)) {
            double limit = s.nextTransition();
            while (time < limit) {
                double dt = Math.min(step, limit) - time;
                double temp = model.step(door.state, power.state, dt);
                time += dt;
                if (time >= step) {
                    System.out.printf("%tF %<tT.%03d\t%.2f\t%.2f\n", (long) time * 1000, (long) (1000 * (time - Math.floor(time))), time, temp);
                    step += DT;
                }
            }
            s.step();
        }
    }
}

package com.tdunning;

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
    public static void main(String[] args) {
        Sim s = new Sim();
        Door door = new Door(s);
        Power power = new Power(s);
        TemperatureModel model = new TemperatureModel();
        model.setTemp(TemperatureModel.EXTERNAL_TEMP);

        double time = 0;
        double step = 0;
        while (time < TimeUnit.SECONDS.convert(100, TimeUnit.DAYS)) {
            double limit = s.nextTransition();
            while (time < limit) {
                double temp = model.step(door.state, power.state, 0.1);
                time += 0.1;
                if (time > step) {
                    System.out.printf("%.2f\t%.2f\n", time, temp);
                    step++;
                }
            }
            s.step();
        }
    }
}

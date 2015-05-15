package com.tdunning;

import java.util.concurrent.TimeUnit;

/**
 * Simulates a freezer.  This is done by having a micro-simulator that emulates
 * the physics of a freezer based on state variables (current temperature) and
 * external controlling variables (power available, door open, door slightly open).
 *
 * The controlling variables are simulated by composing several a discrete event
 * machines where each event is selected according to a simple Markov model.
 * States are defined and characterized by the values of the controlling variables
 * in that state as well as the transition probabilities to another state.
 *
 * The external power system states are:
 *
 * NORMAL_POWER - externalPower is 1, transition to POWER_FAIL is exponential time distribution
 * with mean of months.
 * POWER_FAIL - externalPower is 0, transition to NORMAL_POWER is mixed exponential with
 * means of 1 minute and 2 hours.  90% of transitions are quick.
 *
 * The door states are:
 *
 * CLOSED - thermal coupling to outside world is very low.  Transition to OPEN is exponential
 * mixture with means of 2 minutes and 2 hours.  50% of transitions are quick.
 * OPEN - thermal coupling to outside world is very high.  Transition to either CLOSED (95%)
 * or LEAK (5%) is mixture of uniform between 10 seconds and 2 minutes (99%) or exponential
 * with mean of 30 minutes.
 * LEAK - thermal coupling to outside world is low, but not terribly so.  Transition to open
 * is same as transition from CLOSED.
 *
 * The physical model involves a compressor that turns on if the temperature is > TEMP_MAX
 * and the door state is CLOSED or LEAK.  The compressor turns off if the temperature is <TEMP_MIN.
 *
 * The compressor when on is modeled as a very cold heat sink coupled to the freezer.  The
 * environment is modeled as a heat source coupled to the freezer.  A simple implementation of
 * Newton's law is used to compute the freezer temperature over time.
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
        while (time < TimeUnit.SECONDS.convert(10, TimeUnit.DAYS)) {
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

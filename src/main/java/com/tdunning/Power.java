package com.tdunning;

import java.util.concurrent.TimeUnit;

/**
 * NORMAL_POWER - externalPower is 1, transition to POWER_FAIL is exponential time distribution
 * with mean of months.
 * POWER_FAIL - externalPower is 0, transition to NORMAL_POWER is mixed exponential with
 * means of 1 minute and 2 hours.  90% of transitions are quick.
 */
public class Power {
    public PowerState state = PowerState.NORMAL;

    public Power(Sim s) {
        new Failure(s);
    }

    private class Failure extends Sim.Event {
        private Failure(Sim s) {
            double dt = s.exponential(TimeUnit.SECONDS.convert(7, TimeUnit.DAYS));
            s.schedule(dt, this);
        }

        @Override
        public void run(Sim s) {
            state = PowerState.FAIL;
            new Recovery(s);
        }
    }

    private class Recovery extends Sim.Event {
        private Recovery(Sim s) {
            double mean;
            if (s.uniform() < 0.9) {
                mean = TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES);
            }    else {
                mean = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
            }

            s.schedule(s.exponential(mean), this);
        }

        @Override
        public void run(Sim s) {
            state = PowerState.NORMAL;
            new Failure(s);
        }
    }
}

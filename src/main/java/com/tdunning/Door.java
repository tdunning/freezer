package com.tdunning;

import java.util.concurrent.TimeUnit;

/**
 * CLOSED - thermal coupling to outside world is very low.  Transition to OPEN is exponential
 * mixture with means of 2 minutes and 2 hours.  50% of transitions are quick.
 * OPEN - thermal coupling to outside world is very high.  Transition to either CLOSED (95%)
 * or LEAK (5%) is mixture of uniform between 10 seconds and 2 minutes (99%) or exponential
 * with mean of 30 minutes.
 * LEAK - thermal coupling to outside world is low, but not terribly so.  Transition to open
 * is same as transition from CLOSED.
 */
public class Door {
    DoorState state = DoorState.CLOSED;

    public Door(Sim s) {
        new Open(s, timeUntilOpen(s));
    }

    private double timeUntilOpen(Sim s) {
        // at noon, the rate should be a bit faster than once every 10 minutes.
        // at midnight, it will be much longer
        double rate = Math.sin(Math.PI * s.currentTime() / TimeUnit.SECONDS.convert(1, TimeUnit.DAYS));
        double mean = 600 / (rate * rate * rate * rate + 0.02);
        return 30 + s.exponential(mean);
    }

    private double timeUntilClose(Sim s) {
        return s.uniform(10, 30);
    }

    private class Open extends Sim.Event {
        public Open(Sim s, double dt) {
            s.schedule(dt, this);
        }

        @Override
        public void run(Sim s) {
            state = DoorState.OPEN;
            if (s.uniform() < 0.02) {
                new Leak(s, timeUntilClose(s));
            } else {
                new Close(s, timeUntilClose(s));
            }
        }
    }

    private class Close extends Sim.Event {
        private Close(Sim s, double dt) {
            s.schedule(dt, this);
        }

        @Override
        public void run(Sim s) {
            if (s.uniform() < 0.997) {
                state = DoorState.CLOSED;
            }
            new Open(s, timeUntilOpen(s));
        }
    }

    private class Leak extends Close {
        private Leak(Sim s, double dt) {
            super(s, dt);
        }

        @Override
        public void run(Sim s) {
            state = DoorState.LEAK;
            new Open(s, timeUntilOpen(s));
        }
    }
}

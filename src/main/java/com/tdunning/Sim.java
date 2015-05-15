package com.tdunning;

import com.google.common.collect.Queues;

import java.util.PriorityQueue;
import java.util.Random;

/**
 * Simple discrete event simulator.
 * <p/>
 * Each event schedules 0 or more additional events at future times.
 */
public class Sim {
    private Random gen = new Random();

    public double exponential(double mean) {
        return -mean * Math.log(1 - gen.nextDouble());
    }

    public double uniform() {
        return gen.nextDouble();
    }

    public double uniform(double min, double max) {
        return min + (max - min) * uniform();
    }

    public double currentTime() {
        return time;
    }

    public static abstract class Event implements Comparable<Event> {
        double time;

        public abstract void run(Sim s);

        @Override
        public int compareTo(Event other) {
            return Double.compare(time, other.time);
        }

        public double getTime() {
            return time;
        }

        public void setTime(double t) {
            this.time = t;
        }
    }

    private double time = 0;
    private PriorityQueue<Event> future = Queues.newPriorityQueue();

    public void schedule(double dt, Event event) {
        event.setTime(time + dt);
        future.add(event);
    }

    public void step() {
        Event e = future.poll();
        time = e.getTime();
//        System.out.printf("Event: %.2f,%s\n", time, e);
        e.run(this);
    }

    double nextTransition() {
        return future.peek().getTime();
    }
}

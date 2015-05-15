package com.tdunning;

import junit.framework.TestCase;
import org.junit.Assert;

public class TemperatureModelTest extends TestCase {

    private static final double DT = 0.01;

    public void testClosed() {
        TemperatureModel model = new TemperatureModel();
        simulate(model, DoorState.CLOSED, PowerState.NORMAL, TemperatureModel.TEMP_MIN - 0.1, TemperatureModel.TEMP_MAX, (TemperatureModel.TEMP_MIN + TemperatureModel.TEMP_MAX) / 2);
    }

    public void testLeak() {
        TemperatureModel model = new TemperatureModel();
        // 50 degrees is equilibrium here
        double t = simulate(model, DoorState.LEAK, PowerState.NORMAL, TemperatureModel.TEMP_MIN - 0.1, 50, (TemperatureModel.TEMP_MIN + TemperatureModel.TEMP_MAX) / 2);
        // won't make it all the way, but will definitely go out of bounds
        assertTrue(t > 30);
    }

    public void testOpen() {
        TemperatureModel model = new TemperatureModel();
        // 80 degrees is equilibrium here
        double t = simulate(model, DoorState.OPEN, PowerState.NORMAL, TemperatureModel.TEMP_MIN - 0.1, 80, (TemperatureModel.TEMP_MIN + TemperatureModel.TEMP_MAX) / 2);
        // won't make it all the way, but will definitely go out of bounds
        assertTrue(t > 45);
    }

    private double simulate(TemperatureModel model, DoorState door, PowerState power, double tMin, double tMax, double t0) {
        model.setTemp(t0);

        double step = 0;
        double time = 0;
        double max = -1000;
        double min = 1000;
        double temp = t0;
        for (int i = 0; i <= 100000; i++) {
            max = Math.max(max, temp);
            min = Math.min(min, temp);
            time += DT;
            if (time > step) {
                System.out.printf("%.2f\t%.2f\n", time, temp);
                step++;
            }
            Assert.assertTrue("Temperature out of range", temp >= tMin && temp <= tMax + 0.1);
            temp = model.step(door, power, DT);
        }
        System.out.printf("%.2f, %.2f\n", min, max);
        return temp;
    }
}
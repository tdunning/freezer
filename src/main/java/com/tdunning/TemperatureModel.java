package com.tdunning;

/**
 * Models the temperature of the freezer.
 * <p/>
 * The physical model involves a compressor that turns on if the temperature is > TEMP_MAX
 * and the door state is CLOSED or LEAK.  The compressor turns off if the temperature is <TEMP_MIN.
 * <p/>
 * The compressor when on is modeled as a very cold heat sink coupled to the freezer.  The
 * environment is modeled as a heat source coupled to the freezer.  A simple implementation of
 * Newton's law is used to compute the freezer temperature over time.
 */
public class TemperatureModel {
    public static final double TEMP_MAX = -5;
    public static final double TEMP_MIN = -6;

    public static final double EXTERNAL_TEMP = 30;
    private static final double CLOSED_THERMAL_RESISTANCE = 50000;
    private static final double LEAK_THERMAL_RESISTANCE = CLOSED_THERMAL_RESISTANCE / 7;
    private static final double OPEN_THERMAL_RESISTANCE = CLOSED_THERMAL_RESISTANCE / 20;

    private static final double COMPRESSOR_EFFECTIVE_TEMP = -40;
    private static final double COMPRESSOR_THERMAL_RESISTANCE = CLOSED_THERMAL_RESISTANCE / 2;

    private boolean compressOn = false;
    private double temp = EXTERNAL_TEMP;
    private boolean celsius = true;

    public double step(DoorState door, PowerState power, double dt) {
        double effectiveTemp;
        double effectiveS = 0;

        switch (door) {
            case OPEN:
                effectiveS = 1 / OPEN_THERMAL_RESISTANCE;
                break;
            case LEAK:
                effectiveS = 1 / LEAK_THERMAL_RESISTANCE;
                break;
            case CLOSED:
                effectiveS = 1 / CLOSED_THERMAL_RESISTANCE;
                break;
        }

        if (temp > TEMP_MAX) {
            compressOn = true;
        } else if (temp < TEMP_MIN) {
            compressOn = false;
        }

        if (door == DoorState.OPEN || power == PowerState.FAIL) {
            compressOn = false;
        }

        effectiveTemp = EXTERNAL_TEMP * effectiveS;
        if (compressOn) {
            effectiveS += 1 / COMPRESSOR_THERMAL_RESISTANCE;
            effectiveTemp += COMPRESSOR_EFFECTIVE_TEMP / COMPRESSOR_THERMAL_RESISTANCE;
        }

        effectiveTemp /= effectiveS;
        double deltaTemp = effectiveTemp - temp;
        temp = temp + deltaTemp * (1 - Math.exp(-dt * Math.abs(deltaTemp) * effectiveS));
        return celsius ? temp : 1.8 * temp + 32;
    }

    /**
     * Sets the current temperature of the freezer.  Note that the temperature will be interpreted
     * in whatever temperature scale is in force at the time that this is called.  Thus setting the
     * temperature scale to Fahrenheit *after* setting the temperature may result in a surprise.
     */
    public void setTemp(double temp) {
        if (celsius) {
            this.temp = temp;
        } else {
            this.temp = (temp - 32) / 1.8;
        }
    }

    public void setTempScale(boolean celsius) {
        this.celsius = celsius;
    }
}

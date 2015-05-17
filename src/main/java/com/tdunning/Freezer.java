package com.tdunning;

import com.google.common.base.Preconditions;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.LongOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
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
 */
public class Freezer {

    private static final double DT = 5;

    public static void main(String[] args) throws CmdLineException, FileNotFoundException {
        final Options opts = new Options();
        CmdLineParser parser = new CmdLineParser(opts);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("Usage: " +
                    "    [-output output-file-name]\n" +
                    "    [-fahrenheit] \n" +
                    "    [-time n[smdw]]\n" +
                    "    [-dt seconds-per-output-line\n" +
                    "    [-format CSV|TSV|JSON]\n\n" +
                    "Default is -output - -time 10d -dt 5 -format CSV");
            throw e;
        }


        Sim s = new Sim();
        Door door = new Door(s);
        Power power = new Power(s);
        TemperatureModel model = new TemperatureModel();
        model.setTempScale(!opts.useFahrenheit);

        PrintStream out;
        if (opts.output.equals("-")) {
            out = System.out;
        } else {
            out = new PrintStream(new File(opts.output));
        }
        switch (opts.format) {
            case CSV:
                out.printf("time,timestamp,temp\n");
                break;
            case TSV:
                out.printf("time\ttimestamp\ttemp\n");
                break;
            case JSON:
                out.printf("[\n");
                break;
            case JSON_LINE:
                break;
        }
        double t0 = new GregorianCalendar(2015, 6, 1).getTimeInMillis() / 1000.0;
        double time = t0;
        double step = t0 + opts.dt;
        String jsonSeparator = "";
        while (time < t0 + opts.time) {
            double limit = s.nextTransition();
            while (time < limit) {
                double dt = Math.min(step, limit) - time;
                double temp = model.step(door.state, power.state, dt);
                time += dt;
                if (time >= step) {
                    switch (opts.format) {
                        case CSV:
                            out.printf("\"%tF %<tT.%03d\",%.3f,%.2f\n", (long) time * 1000, (long) (1000 * (time - Math.floor(time))), time, temp);
                            break;
                        case TSV:
                            out.printf("%tF %<tT.%03d\t%.3f\t%.2f\n", (long) time * 1000, (long) (1000 * (time - Math.floor(time))), time, temp);
                            break;
                        case JSON:
                        case JSON_LINE:
                            out.printf("%s{'time'='%tF %<tT.%03d','timestamp'=%.3f,'temp'=%.2f}", jsonSeparator, (long) time * 1000, (long) (1000 * (time - Math.floor(time))), time, temp);
                            switch (opts.format) {
                                case JSON:
                                    jsonSeparator = ",\n";
                                    break;
                                case JSON_LINE:
                                    jsonSeparator = "\n";
                                    break;
                            }
                            break;
                    }
                    step += opts.dt;
                }
            }
            s.step();
        }

        if (opts.format == Format.JSON) {
            out.printf("\n]\n");
        }
    }

    private static class Options {
        @Option(name = "-output")
        String output = "-";

        @Option(name = "-fahrenheit")
        boolean useFahrenheit = false;

        @Option(name = "-time", handler = TimeParser.class)
        long time = TimeUnit.SECONDS.convert(10, TimeUnit.DAYS);

        @Option(name = "-format")
        Format format = Format.CSV;

        @Option(name = "-dt")
        double dt = 5.0;

        public static class TimeParser extends LongOptionHandler {
            public TimeParser(CmdLineParser parser, OptionDef option, Setter<? super Long> setter) {
                super(parser, option, setter);
            }

            @Override
            protected Long parse(String argument) throws NumberFormatException {
                long n = Long.parseLong(argument.replaceAll("[smhdw]?$", ""));

                switch (argument.charAt(argument.length() - 1)) {
                    case 'm':
                        n = TimeUnit.SECONDS.convert(n, TimeUnit.MINUTES);
                        break;
                    case 'h':
                        n = TimeUnit.SECONDS.convert(n, TimeUnit.HOURS);
                        break;
                    case 'w':
                        n = 7 * TimeUnit.SECONDS.convert(n, TimeUnit.DAYS);
                        break;
                    case 's':
                    default:
                        // no suffix leads here
                        break;
                }
                Preconditions.checkArgument(n > 0, "Must have positive time period");
                Preconditions.checkArgument(n < 10 * 365 * TimeUnit.SECONDS.convert(1, TimeUnit.DAYS),
                        "Time period must be less than 10 years");
                return n;
            }
        }
    }

    private enum Format {
        CSV, TSV, JSON_LINE, JSON
    }

}

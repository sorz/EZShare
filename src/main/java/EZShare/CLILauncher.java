package EZShare;

import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Provide CLI argument related common methods used on both Server and Client.
 * Created on 2017/3/22.
 */
abstract class CLILauncher<T> {
    private final String[] args;
    private final String usage;

    CLILauncher(String[] args, String usage) {
        this.args = args;
        this.usage = usage;
    }

    int launch() {
        CommandLine line;
        Options options = getCLIOptions();
        try {
            CommandLineParser parser = new DefaultParser();
            line = parser.parse(options, getArgs());
        } catch (ParseException e) {
            System.err.println("Failed to parse CLI arguments.\n" + e);
            return 1;
        }
        if (line.hasOption("help")) {
            printUsage();
            return 0;
        }

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT [%4$s]\t[%3$s]\t%5$s%6$s%n");
        Level level = line.hasOption("debug") ? Level.FINE : Level.INFO;
        Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers())
                .forEach(h -> h.setLevel(level));
        LogManager.getLogManager().getLogger("").setLevel(level);

        T settings;
        try {
            settings = parseCommandLine(line);
        } catch (ParseException e) {
            System.err.println("Failed to parse CLI arguments.\n" + e);
            System.err.println("Run with -help to show usage. ");
            return 1;
        }
        return run(settings);
    }

    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getUsage(), getCLIOptions());
    }

    private String getUsage() {
        return usage;
    }

    private String[] getArgs() {
        return args;
    }

    Options getCLIOptions() {
        Option help = new Option( "help", "print this message" );
        Option debug = new Option("debug", "print debugging information");
        Options options = new Options();
        options.addOption(help);
        options.addOption(debug);
        return options;
    };

    abstract T parseCommandLine(CommandLine line) throws ParseException;

    abstract int run(T settings);
}

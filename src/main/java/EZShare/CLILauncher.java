package EZShare;

import org.apache.commons.cli.*;

/**
 * Provide CLI argument related common methods used on both Server and Client.
 * Created by xierch on 2017/3/22.
 */
abstract class CLILauncher {
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
            printUsage();
            return 1;
        }
        if (line.hasOption("help")) {
            printUsage();
            return 0;
        }
        return run(line);
    }

    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(usage, getCLIOptions());
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

    abstract int run(CommandLine line);
}

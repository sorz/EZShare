package EZShare;

import org.apache.commons.cli.*;

/**
 * Entrance of server program.
 * Created by xierch on 2017/3/22.
 */
public class Server extends CLILauncher {
    Server(String[] args, String usage) {
        super(args, usage);
    }

    public static void main(String[] args) {
        Server server = new Server(args, "java -cp ezshare.jar EZShare.Server ...");
        System.exit(server.launch());
    }

    @Override
    int run(CommandLine line) {
        System.out.print("Hello, server.");
        return 0;
    }

    @Override
    Options getCLIOptions() {
        Options options = super.getCLIOptions();

        Option hostname = new Option( "advertisedhostname", true,"advertised hostname" );
        Option limit = new Option("connectionintervallimit", true, "connection interval limit in seconds");
        Option interval = new Option("exchangeinterval", true, "exchange interval in seconds");
        Option port = new Option("port", true, "server port, an integer");
        Option secret = new Option("secret", true, "secret");

        options.addOption(hostname);
        options.addOption(limit);
        options.addOption(interval);
        options.addOption(port);
        options.addOption(secret);
        return options;
    }
}

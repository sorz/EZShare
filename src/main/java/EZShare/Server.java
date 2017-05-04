package EZShare;

import EZShare.server.MemoryResourceStorage;
import EZShare.server.ResourceStorage;
import EZShare.server.ServerDaemon;
import EZShare.server.ServerOptions;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entrance of server program.
 * Created on 2017/3/22.
 */
public class Server extends CLILauncher<ServerOptions> {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    private Server(String[] args, String usage) {
        super(args, usage);
    }

    public static void main(String[] args) {
        Server server = new Server(args, "java -cp ezshare.jar EZShare.Server ...");
        System.exit(server.launch());
    }

    @Override
    int run(ServerOptions options) {
        ResourceStorage storage = new MemoryResourceStorage();
        ServerDaemon server = new ServerDaemon(options, storage);
        ServerDaemon secureServer = new ServerDaemon(options, storage, true);
        try {
            server.startInBackground();
            secureServer.startInBackground();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"failed to start server: " + e);
            return -2;
        }
        // TODO: Should program exit when any one of daemons exit?
        server.waitForStop();
        secureServer.waitForStop();
        return 0;
    }

    @Override
    Options getCLIOptions() {
        Options options = super.getCLIOptions();

        Option hostname = new Option("advertisedhostname", true,
                "advertised hostname" );
        Option limit = new Option("connectionintervallimit", true,
                "connection interval limit in seconds");
        Option interval = new Option("exchangeinterval", true,
                "exchange interval in seconds");
        Option port = new Option("port", true,
                "server port, an integer");
        Option sport = new Option("sport", true,
                "server secure port, an integer");
        Option secret = new Option("secret", true,
                "secret");

        limit.setType(Number.class);
        interval.setType(Number.class);
        port.setType(Number.class);
        sport.setType(Number.class);

        options.addOption(hostname);
        options.addOption(limit);
        options.addOption(interval);
        options.addOption(port);
        options.addOption(sport);
        options.addOption(secret);
        return options;
    }

    @Override
    ServerOptions parseCommandLine(CommandLine line) throws ParseException {
        final int DEFAULT_PORT = 3780;
        final int DEFAULT_EXCHANGE_INTERVAL = 10 * 60;  // 10 minutes
        final double DEFAULT_CONNECTION_INTERVAL_LIMIT = 1.0;  // 1 second
        final int DEFAULT_SECRET_ENTROPY_BITS = 128;

        String hostname = line.getOptionValue("advertisedhostname");
        if (hostname == null) {
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                throw new ParseException("failed to get hostname, please specify one.");
            }
        }
        double limit = DEFAULT_CONNECTION_INTERVAL_LIMIT;
        if (line.hasOption("connectionintervallimit"))
            limit = ((Number) line.getParsedOptionValue("connectionintervallimit")).doubleValue();
        if (limit < 0)
            throw new ParseException("connection interval limit cannot be negative");

        int interval = DEFAULT_EXCHANGE_INTERVAL;
        if (line.hasOption("exchangeinterval"))
            interval = ((Number) line.getParsedOptionValue("exchangeinterval")).intValue();
        if (interval <= 0)
            throw new ParseException("exchange interval must be positive.");

        int port = DEFAULT_PORT;
        if (line.hasOption("port"))
            port = parsePortNumber((Number) line.getParsedOptionValue("port"));
        int sport = DEFAULT_SECURE_PORT;
        if (line.hasOption("sport"))
            sport = parsePortNumber((Number) line.getParsedOptionValue("sport"));

        String secret = line.getOptionValue("secret");
        if (secret == null) {
            secret = new BigInteger(DEFAULT_SECRET_ENTROPY_BITS, new SecureRandom()).toString(32);
            LOGGER.info("random secret generated: " + secret);
        }

        return new ServerOptions(hostname, limit, interval, port, sport, secret);
    }

}

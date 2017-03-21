package EZShare;

import org.apache.commons.cli.*;

/**
 * Entrance of client program.
 * Created by xierch on 2017/3/22.
 */
public class Client {
    public static void main(String[] args) {
        Options options = getCLIOptions();
        CommandLine line;
        try {
            CommandLineParser parser = new DefaultParser();
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Failed to parse CLI arguments.\n" + e);
            printUsage(options);
            System.exit(1);
            return;
        }
        if (line.hasOption("help"))
            printUsage(options);

    }

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -cp ezshare.jar EZShare.Client ...", options);
    }

    private static Options getCLIOptions() {
        Option help = new Option( "help", "print this message" );
        Option channel = new Option("channel", true, "channel");
        Option debug = new Option("debug", "print debugging information");
        Option description = new Option("description", true, "resource description");
        Option exchange = new Option("exchange", "exchange server list with server");
        Option fetch = new Option("fetch", "fetch resources from server");
        Option host = new Option("host", true, "server host, a domain name or IP address");
        Option name = new Option("name", true, "resource name");
        Option owner = new Option("owner", true, "owner");
        Option port = new Option("port", true, "server port, an integer");
        Option publish = new Option("publish", "publish resource on server");
        Option query = new Option("query", "query for resources from server");
        Option remove = new Option("remove", "remove resource from server");
        Option secret = new Option("secret", true, "secret");
        Option servers = new Option("servers", true, "server list, host1:port1,host2:port2,...");
        Option share = new Option("share", "share resource on server");
        Option tags = new Option("tags", true, "resource tags, tag1,tag2,tag3,...");
        Option uri = new Option("uri", true, "resource URI");

        Options options = new Options();
        options.addOption(help);
        options.addOption(channel);
        options.addOption(debug);
        options.addOption(description);
        options.addOption(exchange);
        options.addOption(fetch);
        options.addOption(host);
        options.addOption(name);
        options.addOption(owner);
        options.addOption(port);
        options.addOption(publish);
        options.addOption(query);
        options.addOption(remove);
        options.addOption(secret);
        options.addOption(servers);
        options.addOption(share);
        options.addOption(tags);
        options.addOption(uri);
        return options;
    }
}

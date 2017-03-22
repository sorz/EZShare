package EZShare;

import EZShare.client.ClientOptions;
import org.apache.commons.cli.*;

/**
 * Entrance of client program.
 * Created by xierch on 2017/3/22.
 */
public class Client extends CLILauncher<ClientOptions> {
    private Client(String[] args, String usage) {
        super(args, usage);
    }

    public static void main(String[] args) {
        Client client = new Client(args, "java -cp ezshare.jar EZShare.Client ...");
        System.exit(client.launch());
    }

    @Override
    int run(ClientOptions options) {
        System.out.print("Hello, client.");
        return 0;
    }

    @Override
    Options getCLIOptions() {
        Options options = super.getCLIOptions();

        Option channel = new Option("channel", true, "channel");
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

        options.addOption(channel);
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

    @Override
    ClientOptions parseCommandLine(CommandLine line) throws ParseException {
        return null;
    }

}

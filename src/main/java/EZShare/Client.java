package EZShare;

import EZShare.client.ClientMain;
import EZShare.client.ClientOptions;
import EZShare.entities.Command;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Entrance of client program.
 * Created on 2017/3/22.
 */
public class Client extends CLILauncher<ClientOptions> {
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName());

    private Client(String[] args, String usage) {
        super(args, usage);
    }

    public static void main(String[] args) {
        Client client = new Client(args, "java -cp ezshare.jar EZShare.Client ...");
        System.exit(client.launch());
    }

    @Override
    int run(ClientOptions options) {
        try {
            ClientMain.execute(options);
        } catch (IOException e) {
            LOGGER.warning("I/O error: " + e);
            return -2;
        }
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
        Option secure = new Option("secure", "make a secure connection");

        port.setType(Number.class);

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
        options.addOption(secure);
        return options;
    }

    @Override
    ClientOptions parseCommandLine(CommandLine line) throws ParseException {
        ClientOptions options = new ClientOptions();
        Command.CMD[] commands = Arrays.stream(Command.CMD.values())
                .filter((cmd -> line.hasOption(cmd.name().toLowerCase())))
                .toArray(Command.CMD[]::new);
        if (commands.length == 0)
            throw new ParseException("must specify a command");
        else if (commands.length > 1)
            throw new ParseException(String.format("conflict command -%s and -%s",
                    commands[0].name(), commands[1].name()));
        options.setCommand(commands[0]);

        options.setChannel(line.getOptionValue("channel", ""))
                .setDescription(line.getOptionValue("description", ""))
                .setHost(line.getOptionValue("host", "localhost"))
                .setName(line.getOptionValue("name", ""))
                .setOwner(line.getOptionValue("owner", ""))
                .setSecret(line.getOptionValue("secret"))
                .setSecure(line.hasOption("secure"));

        int port = DEFAULT_PORT;
        if (line.hasOption("port"))
            port = parsePortNumber((Number) line.getParsedOptionValue("port"));
        options.setPort(port);

        if (line.hasOption("servers")) {
            List<Pair<String, Integer>> servers = new ArrayList<>();
            for (String server : line.getOptionValue("servers").split(",")) {
                String[] hostPort = server.trim().split(":");
                String host = hostPort[0].trim();
                int portNo = DEFAULT_PORT;
                if (hostPort.length >= 2) {
                    try {
                        portNo = Integer.parseInt(hostPort[1].trim());
                    } catch (NumberFormatException e) {
                        throw new ParseException("malformed port number on -servers: " + e);
                    }
                    if ((portNo & ~0xffff) != 0)
                        throw new ParseException("port number must within 0 to 65535.");
                }
                servers.add(Pair.of(host, portNo));
            }
            options.setServers(servers);
        }

        List<String> tags = Arrays.stream(line.getOptionValue("tags", "").split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
        options.setTags(tags);

        if (line.hasOption("uri")) {
            URI uri;
            try {
                uri = new URI(line.getOptionValue("uri"));
            } catch (URISyntaxException e) {
                throw new ParseException("malformed URI: " + e);
            }
            options.setUri(uri);
        }

        if ("*".equals(options.getOwner()))
            throw new ParseException("owner cannot be \"*\".");
        switch (options.getCommand()) {
            case PUBLISH:
                if (options.getUri() == null)
                    throw new ParseException("must specify -uri");
                if ("file".equals(options.getUri().getScheme()))
                    throw new ParseException("URI cannot be a file://.");
                break;
            case REMOVE:
                if (options.getUri() == null)
                    throw new ParseException("must specify -uri");
                break;
            case SHARE:
                if (options.getSecret() == null)
                    throw new ParseException("must specify -secret");
                // no break: continue to check FETCH's
            case FETCH:
                if (options.getUri() == null)
                    throw new ParseException("must specify -uri");
                if (!"file".equals(options.getUri().getScheme()))
                    throw new ParseException("URI must be a file://.");
                break;
            case QUERY:
                break;
            case EXCHANGE:
                if (options.getServers() == null)
                    throw new ParseException("must specify -servers");
                break;
            default:
                break;
        }

        return options;
    }

}

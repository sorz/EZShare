package EZShare.client;

import EZShare.entities.Publish;
import EZShare.entities.Resource;
import EZShare.entities.Response;
import EZShare.networking.EZInputOutput;
import javafx.util.Pair;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main class for EZShare client.
 * Created by xierch on 2017/3/23.
 */
public class ClientMain {
    private final static Logger LOGGER = Logger.getLogger(ClientMain.class.getName());

    // TODO: use enums?
    private final static String CMD_PUBLISH = "PUBLISH";
    private final static String CMD_REMOVE = "REMOVE";
    private final static String CMD_SHARE = "SHARE";
    private final static String CMD_QUERY = "QUERY";
    private final static String CMD_FETCH = "FETCH";
    private final static String CMD_EXCHANGE = "EXCHANGE";


    private final EZInputOutput io;


    private ClientMain(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        io = new EZInputOutput(socket);
    }

    public static void execute(ClientOptions options) throws IOException {
        LOGGER.fine(String.format("%s to %s:%d...", options.getCommand(),
                options.getHost(), options.getPort()));
        ClientMain client = new ClientMain(options.getHost(), options.getPort());
        String cmd = options.getCommand();
        try {
            if (cmd.equals(CMD_EXCHANGE)) {
                client.exchange(options.getServers());
            } else {
                Resource resource = new Resource();
                resource.setChannel(options.getChannel());
                resource.setDescription(options.getDescription());
                resource.setName(options.getName());
                resource.setOwner(options.getOwner());
                resource.setTags(options.getTags());
                resource.setUri(options.getUri().toString());
                switch (cmd) {
                    case CMD_PUBLISH:
                        client.publish(resource);
                        break;
                    case CMD_REMOVE:
                        break;
                    case CMD_SHARE:
                        break;
                    case CMD_FETCH:
                        break;
                    default:
                        break;
                }
            }


        } finally {
            client.close();
        }


    }

    private Response publish(Resource resource) throws IOException {
        io.sendJSON(new Publish(resource));
        io.readCommand();
        return null;
    }

    private Response exchange(List<Pair<String, Integer>> servers) {
        return null;
    }

    private void close() {
        io.close();
    }

}

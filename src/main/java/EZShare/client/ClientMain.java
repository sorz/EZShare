package EZShare.client;

import EZShare.entities.*;
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

    private final EZInputOutput io;


    private ClientMain(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        io = new EZInputOutput(socket);
    }

    public static void execute(ClientOptions options) throws IOException {
        LOGGER.fine(String.format("%s to %s:%d...", options.getCommand(),
                options.getHost(), options.getPort()));
        ClientMain client = new ClientMain(options.getHost(), options.getPort());
        try {
            if (Command.CMD.EXCHANGE == options.getCommand()) {
                client.exchange(options.getServers());
            } else {
                Resource resource = new Resource();
                resource.setChannel(options.getChannel());
                resource.setDescription(options.getDescription());
                resource.setName(options.getName());
                resource.setOwner(options.getOwner());
                resource.setTags(options.getTags());
                resource.setUri(options.getUri().toString());
                switch (options.getCommand()) {
                    case PUBLISH:
                        System.out.println(client.publish(resource));
                        break;
                    case REMOVE:
                        System.out.println(client.remove(resource));
                        break;
                    case SHARE:
                        System.out.println(client.share(resource, options.getSecret()));
                        break;
                    case FETCH:
                        break;
                    case EXCHANGE:
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
        return io.readResponse();
    }

    private Response remove(Resource resource) throws IOException {
        io.sendJSON(new Remove(resource));
        return io.readResponse();
    }

    private Response share(Resource resource, String secret) throws IOException {
        io.sendJSON(new Share(resource, secret));
        return io.readResponse();
    }

    private Response exchange(List<Pair<String, Integer>> servers) {
        return null;
    }

    private void close() {
        io.close();
    }

}

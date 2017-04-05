package EZShare.client;

import EZShare.entities.*;
import EZShare.networking.EZInputOutput;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
            Command cmdToSend;
            if (Command.CMD.EXCHANGE == options.getCommand()) {
                // The only command that have no resource/template.
                List<Server> servers = options.getServers()
                        .stream()
                        .map(pair -> new Server(pair.getLeft(), pair.getRight()))
                        .collect(Collectors.toList());
                cmdToSend = new Exchange(servers);
            } else {
                Resource resource = new Resource();
                resource.setChannel(options.getChannel());
                resource.setDescription(options.getDescription());
                resource.setName(options.getName());
                resource.setOwner(options.getOwner());
                resource.setTags(options.getTags());
                resource.setUri(options.getUriString());
                switch (options.getCommand()) {
                    case PUBLISH:
                        cmdToSend = new Publish(resource);
                        break;
                    case REMOVE:
                        cmdToSend = new Remove(resource);
                        break;
                    case SHARE:
                        cmdToSend = new Share(resource, options.getSecret());
                        break;
                    case QUERY:
                        cmdToSend = new Query(resource, true);
                        break;
                    case FETCH:
                        cmdToSend = new Fetch(resource);
                        break;
                    default:
                        throw new IllegalStateException("Unknown command");
                }
            }
            Response response = client.sendCommand(cmdToSend);
            // TODO: better format of output
            System.out.println(response);
            if (!response.isSuccess())
                return;

            if (options.getCommand() == Command.CMD.QUERY) {
                // TODO: better format of output
                client.readResources(System.out::println);
            } else if (options.getCommand() == Command.CMD.FETCH) {
                Resource resource = client.readResource();
                // TODO: better format of output
                System.out.println(resource);
                // Try to derive a filename from URI and resource name,
                // if failed, get a temp file path.
                String filename = getFilenameFromURI(resource.getUri());
                if (filename.isEmpty())
                    filename = resource.getName();
                filename = filename.replaceAll("(\\\\|\\.\\.|/)", "_");
                Path path = new File(filename).toPath();
                if (!Files.isWritable(path)|| Files.exists(path))
                    path = Files.createTempFile("ezshare-", ".bin");
                client.readToFile(path, resource.getResourceSize());
            }
        } finally {
            client.close();
        }
    }

    private Response sendCommand(Command command) throws IOException {
        io.sendJSON(command);
        return io.readResponse();
    }

    private void readResources(Consumer<Resource> consumer) throws IOException {
        io.readResources(consumer);
    }

    private Resource readResource() throws IOException {
        return io.readJSON(Resource.class);
    }

    private void readToFile(Path path, long size) throws IOException {
        OutputStream output = Files.newOutputStream(path);
        io.readBinaryTo(output, size);
        output.flush();
        output.close();
    }

    private static String getFilenameFromURI(String uri) {
        int index = uri.lastIndexOf('/');
        if (index == -1)
            return "";
        return uri.substring(index);
    }

    private void close() {
        io.close();
    }

}

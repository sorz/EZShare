package EZShare.client;

import EZShare.entities.*;
import EZShare.networking.EZInputOutput;
import EZShare.networking.SecurityHelper;
import EZShare.networking.SecuritySetupException;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main class for EZShare client.
 * Created on 2017/3/23.
 */
public class ClientMain {
    private final static Logger LOGGER = Logger.getLogger(ClientMain.class.getName());

    private final EZInputOutput io;


    private ClientMain(String host, int port, boolean secure)
            throws IOException, SecuritySetupException {
        Socket socket;
        if (secure)
            socket = SecurityHelper.getClient()
                    .getSSLContext().getSocketFactory()
                    .createSocket(host, port);
        else
            socket = new Socket(host, port);
        io = new EZInputOutput(socket);
    }

    public static void execute(ClientOptions options)
            throws IOException, SecuritySetupException {
        LOGGER.fine(String.format("%s to %s:%d...", options.getCommand(),
                options.getHost(), options.getPort()));
        ClientMain client = new ClientMain(options.getHost(), options.getPort(),
                options.isSecure());
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
                    case SUBSCRIBE:
                        cmdToSend = new Subscribe(resource, true);
                        break;
                    default:
                        throw new IllegalStateException("Unknown command");
                }
            }
            client.io.sendJSON(cmdToSend);
            Response response = client.io.readResponse();
            // TODO: better format of output
            System.out.println(response);
            if (!response.isSuccess())
                return;

            if (cmdToSend instanceof Query) {
                int size = client.io.readResources(System.out::println);
                if (size == 0)
                    System.out.println("No resource found.");
                else
                    System.out.printf("%s resource(s) found in total.\n", size);

            } else if (cmdToSend instanceof Subscribe) {
                String id = ((Subscribe) cmdToSend).getId();
                new Thread(() -> {
                    System.out.printf("Press ENTER to stop.");
                    new Scanner(System.in).nextLine();
                    try {
                        client.io.sendJSON(new Unsubscribe(id));
                    } catch (IOException e) {
                        LOGGER.warning("I/O error: " + e);
                        System.exit(2);
                    }
                }).start();
                client.io.setTimeout(0);
                int size = client.io.readResources(System.out::println);
                System.out.printf("%s resource(s) received in total.\n", size);

            } else if (cmdToSend instanceof Fetch) {
                Resource resource = client.io.readJSON(Resource.class);
                System.out.println(resource);
                // Try to derive a filename from URI or resource name.
                String filename = getFilenameFromURI(resource.getUri());
                if (filename.isEmpty()) {
                    filename = resource.getName().isEmpty() ? "unnamed" : resource.getName();
                    filename = "ezfile-" + filename + ".bin";
                }
                // Replace all "..", "\" and "/" to "_".
                filename = filename.replaceAll("(\\\\|\\.\\.|/)", "_");
                Path path = new File(filename).getAbsoluteFile().toPath();
                // Save to temp dir if current dir cannot write or file already exists.
                if (!Files.isWritable(path.getParent()) || Files.exists(path)) {
                    String dir = Files.createTempDirectory("ezshare-").toString();
                    path = new File(dir, filename).toPath();
                }
                System.out.printf("Saving file to %s ...\n", path);
                long size = client.readToFile(path, resource.getResourceSize());
                System.out.printf("%,d bytes received, done.\n", size);
            }
        } finally {
            client.close();
        }
    }

    private long readToFile(Path path, long size) throws IOException {
        OutputStream output = Files.newOutputStream(path);
        long received = io.readBinaryTo(output, size);
        output.flush();
        output.close();
        return received;
    }

    private static String getFilenameFromURI(String uri) {
        int index = uri.lastIndexOf('/');
        if (index == -1)
            return "";
        return uri.substring(index + 1);
    }

    private void close() {
        io.close();
    }

}

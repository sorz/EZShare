package EZShare.server;

import EZShare.entities.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Main class for server.
 * Created by xierch on 2017/3/23.
 */
public class ServerDaemon implements ClientCommandHandler {
    private final static Logger LOGGER = Logger.getLogger(ServerDaemon.class.getName());

    private final ServerOptions options;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ResourceStorage resourceStorage = new MemoryResourceStorage();
    private ServerSocket serverSocket;
    private boolean isRunning;


    public ServerDaemon(ServerOptions options) {
        this.options = options;
    }

    public void start() throws IOException {
        LOGGER.info("bind on port " + options.getPort());
        serverSocket = new ServerSocket(options.getPort());
        isRunning = true;
    }

    public void runForever() throws IOException {
        LOGGER.info("server is running");
        while (isRunning) {
            Socket socket = serverSocket.accept();
            LOGGER.info("accept connection from " + socket.getInetAddress());
            try {
                Client client = new Client(socket, this);
                executorService.submit(client);
            } catch (IOException e) {
                LOGGER.warning("error on handle client " + socket.getInetAddress() + ": " + e);
            }
        }
    }

    public void stop() {
        LOGGER.info("stopping");
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public void doPublish(Publish cmd) throws CommandHandleException {
        Resource resource = cmd.getResource();
        if (resource == null)
            throw new CommandHandleException("missing resource");
        URI uri;
        try {
            uri = resource.getNormalizedUri();
        } catch (URISyntaxException e) {
            LOGGER.info("fail to parse URI: " + e);
            throw new CommandHandleException("invalid resource");
        }
        if (uri.getScheme().equals("file")) {
            LOGGER.info("cannot publish with a file:// URI");
            throw new CommandHandleException("invalid resource");
        }
        if (resource.getOwner().equals("*")) {
            LOGGER.info("cannot publish with user \"*\"");
            throw new CommandHandleException("invalid resource");
        }
        synchronized (resourceStorage) {
            Resource oldResource = resourceStorage.get(resource.getChannel(), uri);
            if (oldResource != null && !oldResource.getOwner().equals(resource.getOwner()))
                throw new CommandHandleException("cannot publish resource");
            resourceStorage.put(resource.getChannel(), uri, resource);
            LOGGER.fine(String.format("new resource (%s, %s) published.",
                    resource.getChannel(), uri));
        }
    }

    @Override
    public void doRemove(Remove cmd) throws CommandHandleException {

    }

    @Override
    public void doShare(Share cmd) throws CommandHandleException {

    }

    @Override
    public List<Resource> doQuery(Query cmd) throws CommandHandleException {
        return null;
    }

    @Override
    public Pair<Resource, InputStream> doFetch(Fetch cmd) throws CommandHandleException {
        return null;
    }
}

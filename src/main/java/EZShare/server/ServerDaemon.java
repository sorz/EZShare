package EZShare.server;

import EZShare.entities.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
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

    /**
     * Get resource from command. That resource will have a legal URI and
     * non-"*" owner.
     * @param command that must contain a Resource object.
     * @return A verified resource from this command.
     * @throws CommandHandleException when command's resource is null, blank
     * or invalid URI, "*" as resource owner.
     */
    private Resource verifyThenGetResource(CommandWithResource command)
            throws CommandHandleException {
        Resource resource = command.getResource();
        if (resource == null)
            throw new CommandHandleException("missing resource");
        URI uri = resource.getNormalizedUri();
        if (uri == null) {
            LOGGER.info("fail to parse URI");
            throw new CommandHandleException("invalid resource");
        }
        if (resource.getOwner().equals("*")) {
            LOGGER.info("cannot publish with user \"*\"");
            throw new CommandHandleException("invalid resource");
        }
        return resource;
    }

    @Override
    public void doPublish(Publish cmd) throws CommandHandleException {
        Resource resource = verifyThenGetResource(cmd);
        URI uri = resource.getNormalizedUri();
        if ("file".equals(uri.getScheme())
                || uri.getAuthority() == null
                || !uri.isAbsolute()) {
            LOGGER.fine("URI illegal to publish");
            throw new CommandHandleException("invalid resource");
        }

        synchronized (resourceStorage) {
            if (!resourceStorage.updateResource(resource))
                throw new CommandHandleException("cannot publish resource");
            LOGGER.fine(String.format("new resource (%s, %s) published.",
                    resource.getChannel(), uri));
        }
    }

    @Override
    public void doRemove(Remove cmd) throws CommandHandleException {
        Resource resource = verifyThenGetResource(cmd);
        URI uri = resource.getNormalizedUri();
        synchronized (resourceStorage) {
            Resource oldResource = resourceStorage.get(resource.getChannel(), uri);
            if (oldResource == null || !oldResource.getOwner().equals(resource.getOwner()))
                throw new CommandHandleException("cannot remove resource");
            resourceStorage.remove(resource.getChannel(), uri);
            LOGGER.fine(String.format("resource (%s, %s) removed.",
                    resource.getChannel(), uri));
        }
    }

    @Override
    public void doShare(Share cmd) throws CommandHandleException {
        if (cmd.getSecret() == null || cmd.getResource() == null)
            // redundant checking & fuzzy error message here,
            // but the requirement enforce that.
            throw new CommandHandleException("missing resource and/or secret");
        if (!options.getSecret().equals(cmd.getSecret()))
            throw new CommandHandleException("incorrect secret");
        Resource resource = verifyThenGetResource(cmd);
        URI uri = resource.getNormalizedUri();
        if (!"file".equals(uri.getScheme())
                || uri.getAuthority() != null
                || !uri.isAbsolute()) {
            LOGGER.fine("URI illegal to publish");
            throw new CommandHandleException("invalid resource");
        }
        if (!new File(uri).canRead()) {
            LOGGER.fine("URI file cannot be read");
            throw new CommandHandleException("invalid resource");
        }

        synchronized (resourceStorage) {
            if (!resourceStorage.updateResource(resource))
                throw new CommandHandleException("cannot share resource");
            LOGGER.fine(String.format("new resource (%s, %s) shared.",
                    resource.getChannel(), uri));
        }
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

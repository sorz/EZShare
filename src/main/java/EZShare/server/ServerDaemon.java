package EZShare.server;

import EZShare.entities.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Main class for server.
 * Created by xierch on 2017/3/23.
 */
public class ServerDaemon implements ClientCommandHandler {
    private final static Logger LOGGER = Logger.getLogger(ServerDaemon.class.getName());

    private final ServerOptions options;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final InterServerService interServerService;
    private final ResourceStorage resourceStorage = new MemoryResourceStorage();
    private ServerSocket serverSocket;
    private boolean isRunning;


    public ServerDaemon(ServerOptions options) {
        this.options = options;
        interServerService = new InterServerService(options.getExchangeInterval());
    }

    /**
     * Bind to port.
     * @throws IOException if failed to bind the port.
     */
    public void start() throws IOException {
        LOGGER.info("bind on port " + options.getPort());
        serverSocket = new ServerSocket(options.getPort());
        isRunning = true;
        executorService.submit(interServerService);
    }

    /**
     * Never return unless stop() be invoke or exceptions throw.
     * Must invoke after start().
     * @throws IOException if error on accept socket.
     */
    public void serveForever() throws IOException {
        LOGGER.info("server is running");
        long lastAcceptTimeMillis = (long) (- options.getConnectionIntervalLimit() * 1000);
        while (isRunning) {
            Socket socket = serverSocket.accept();
            if (System.currentTimeMillis() - lastAcceptTimeMillis <
                    options.getConnectionIntervalLimit() * 1000) {
                LOGGER.info("reject connection from " + socket.getInetAddress());
                IOUtils.closeQuietly(socket);
                continue;
            }
            lastAcceptTimeMillis = System.currentTimeMillis();
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
        interServerService.stop();
        IOUtils.closeQuietly(serverSocket);
    }

    /**
     * Copy a resource and set its owner to "*" if exists, and also set the
     * ezServer.
     * @param resource to copy.
     * @return copied resource.
     */
    private Resource copyAsAnonymousResource(Resource resource) {
        Resource newResource = new Resource(resource);
        newResource.setEzserver(String.format("%s:%d",
                options.getHostname(), options.getPort()));
        if (!resource.getOwner().isEmpty())
            newResource.setOwner("*");
        return newResource;
    }

    /**
     * Get resource from command. That resource will have a legal URI and
     * non-"*" owner. Resource's URI will be canonicalized.
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
        if (resource.getOwner().equals("*")) {
            LOGGER.info("cannot publish with user \"*\"");
            throw new CommandHandleException("invalid resource");
        }
        URI uri = resource.getNormalizedUri();
        if (uri == null) {
            LOGGER.info("fail to parse URI");
            throw new CommandHandleException("invalid resource");
        }
        resource.setUri(uri.toString());
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
    public int doQuery(Query cmd, Consumer<Void> ok, Consumer<Resource> consumer)
            throws CommandHandleException {
        Resource template = cmd.getResourceTemplate();
        if (template == null)
            throw new CommandHandleException("missing resourceTemplate");
        if (template.getNormalizedUri() != null)
            template.setUri(template.getNormalizedUri().toString());
        // Tell caller the command is valid and we are ready to send resources.
        ok.accept(null);

        Counter<Resource> counter = new Counter<>();
        if (cmd.isRelay())
            interServerService.queryAll(cmd, r -> {
                counter.count(r);
                consumer.accept(r);
            });
        synchronized (resourceStorage) {
            resourceStorage.templateQuery(template)
                    .map(this::copyAsAnonymousResource)
                    .map(counter::count)
                    .forEach(consumer);
        }
        return counter.num;
    }

    @Override
    public Pair<Resource, InputStream> doFetch(Fetch cmd) throws CommandHandleException {
        Resource template = cmd.getResourceTemplate();
        if (template == null)
            throw new CommandHandleException("missing resourceTemplate");
        URI uri = template.getNormalizedUri();
        if (uri == null || !"file".equals(uri.getScheme())) {
            LOGGER.fine("URI illegal to fetch");
            throw new CommandHandleException("invalid resourceTemplate");
        }
        Resource resource;
        synchronized (resourceStorage) {
            resource = resourceStorage.get(template.getChannel(), uri);
        }
        if (resource == null) {
            LOGGER.fine("resource not found");
            // TODO: can we throw "resource not found" instead?
            throw new CommandHandleException("invalid resourceTemplate");
        }
        resource = copyAsAnonymousResource(resource);
        InputStream inputStream;
        try {
            File file = new File(uri);
            resource.setResourceSize(file.length());
            inputStream = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            LOGGER.warning(String.format("resource (%s, %s) file not found",
                    template.getChannel(), uri));
            // TODO: can we throw "file not found" instead?
            throw new CommandHandleException("invalid resourceTemplate");
        }
        return Pair.of(resource, inputStream);
    }

    @Override
    public void doExchange(Exchange cmd) throws CommandHandleException {
        if (cmd.getServerList() == null || cmd.getServerList().isEmpty())
            throw new CommandHandleException("missing or invalid server list");
        if (!cmd.getServerList().stream().allMatch(Server::isValid))
            throw new CommandHandleException("invalid server record found");
        interServerService.addServers(cmd.getServerList());
    }

    private static class Counter<T> {
        int num;
        synchronized T count(T obj) {
                num++;
            return obj;
        }
    }
}

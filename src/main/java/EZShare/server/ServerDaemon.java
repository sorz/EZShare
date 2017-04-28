package EZShare.server;

import EZShare.entities.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main class for server.
 * Created on 2017/3/23.
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
        interServerService = new InterServerService(
                options.getHostname(), options.getPort(),
                options.getExchangeInterval() * 1000);
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
        final long intervalLimitMillis = (long) (options.getConnectionIntervalLimit() * 1000);
        Hashtable<String, Long> lastAcceptTimestamps = new Hashtable<>();
        long lastCleanupTable = System.currentTimeMillis();

        while (isRunning) {
            Socket socket = serverSocket.accept();

            // Reject if that IP connecting too frequently.
            long lastAcceptTimeMillis = lastAcceptTimestamps.getOrDefault(
                    socket.getInetAddress().getHostAddress(), -intervalLimitMillis);
            if (System.currentTimeMillis() - lastAcceptTimeMillis < intervalLimitMillis) {
                LOGGER.info("reject connection from " + socket.getInetAddress());
                IOUtils.closeQuietly(socket);
                continue;
            }
            lastAcceptTimestamps.put(socket.getInetAddress().getHostAddress(), System.currentTimeMillis());
            // Clean up lastAcceptTimestamps if we have many entries in it.
            if (lastAcceptTimestamps.size() > 64
                    && System.currentTimeMillis() - lastCleanupTable > intervalLimitMillis) {
                lastAcceptTimestamps = new Hashtable<>(lastAcceptTimestamps.entrySet()
                        .stream().filter(e -> System.currentTimeMillis() - e.getValue() < intervalLimitMillis)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                lastCleanupTable = System.currentTimeMillis();
            }

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
        // Return local resource first.
        // If client do not want wait too long and disconnect in advance,
        // this ensure them do not miss local resources.
        synchronized (resourceStorage) {
            resourceStorage.templateQuery(template)
                    .map(this::copyAsAnonymousResource)
                    .map(counter::count)
                    .forEach(consumer);
        }
        if (cmd.isRelay()) {
            // Although ignore query with non-blank channel/owner is more reasonable,
            // the specification enforce to set them empty and relay.
            // We follow the specification here.
            Resource anonymousTemplate = new Resource(template);
            anonymousTemplate.setChannel("");
            anonymousTemplate.setOwner("");
            Query query = new Query(anonymousTemplate, false);
            interServerService.queryAll(query, r -> {
                counter.count(r);
                consumer.accept(r);
            });
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
            throw new CommandHandleException("file not found on server");
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

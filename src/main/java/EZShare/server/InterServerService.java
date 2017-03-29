package EZShare.server;

import EZShare.entities.*;
import EZShare.networking.EZInputOutput;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Provide following services:
 *   - Maintenance a list of remote servers.
 *      - Exchange the list to other servers periodically.
 *      - Remove lost servers from the list.
 *      - Allow add servers via addServers() method.
 *   - Send query to each of servers on the list, collect & return results.
 *
 *   This class do not receive EXCHANGE command (it only send that), incoming
 *   EXCHANGE command must be handle elsewhere and only to add servers with
 *   addServers() method.
 *
 * Created by xierch on 2017/3/29.
 */
public class InterServerService implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(InterServerService.class.getName());
    // We may not want server start too many threads on user's query request.
    private final static int MAX_QUERY_THREAD = 32;

    final private Set<Server> servers = new HashSet<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_QUERY_THREAD);

    private long exchangeIntervalMillis;
    private boolean isRunning;


    /**
     * Create a ExchangeService.
     * @param exchangeIntervalMillis wait for each that milliseconds, initiates
     *                               an EXCHANGE command to random server on
     *                               the list.
     */
    public InterServerService(long exchangeIntervalMillis) {
        this.exchangeIntervalMillis = exchangeIntervalMillis;
    }


    /**
     * Exchange the list with other servers on specified time interval.
     * Never return unless stop() is invoked.
     */
    @Override
    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(exchangeIntervalMillis);
            } catch (InterruptedException e) {
                LOGGER.info("interrupted, stop exchange service");
                stop();
            }
            Server server = getRandomServer();
            if (server == null) {
                LOGGER.fine("no remote server, skip exchange");
                continue;
            }
            try {
                sendExchangeCommand(server);
            } catch (IOException e) {
                synchronized (this.servers) {
                    this.servers.remove(server);
                }
                LOGGER.fine("fail to exchange with " + server + ", removed");
            }

        }
    }

    /**
     * Randomly choose a server from servers.
     * @return a server or null if servers is empty.
     */
    @Nullable
    private Server getRandomServer() {
        synchronized (servers) {
            return servers.stream()
                    .skip((int) (servers.size() * Math.random()))
                    .findFirst().orElse(null);
        }
    }

    /**
     * Send exchange command with server list to other server.
     * @param server that exchange send to.
     * @throws IOException if error on networking.
     */
    private void sendExchangeCommand(Server server) throws IOException {
        EZInputOutput io = new EZInputOutput(server);
        Exchange exchange;
        synchronized (servers) {
            exchange = new Exchange(new ArrayList<>(servers));
        }
        io.sendJSON(exchange);
        Response response = io.readResponse();
        if (!response.isSuccess())
            LOGGER.fine(String.format("failed to exchange with %s: %s", server, response));
        else
            LOGGER.fine("exchange send successfully to " + server);
    }

    /**
     * Add servers to the internal remote server list.
     * @param servers to add. Already exist server will be silently ignored.
     */
    public void addServers(@NotNull Collection<Server> servers) {
        synchronized (this.servers) {
            if (this.servers.addAll(servers))
                LOGGER.info("server list updated");
            else
                LOGGER.fine("server list kept no changed");
        }
    }

    /**
     * Send query to all servers in internal server list.
     * When finish, the thread that invoke this method will be interrupted.
     *
     * Multi-threading is taken to make query in parallel.
     *
     * @param query to send.
     * @return An BlockingQueue immediately, resources returned by server will
     * put in that queue.
     */
    public BlockingQueue<Resource> queryAll(@NotNull Query query) {
        Thread consumerThread = Thread.currentThread();
        BlockingQueue<Resource> queue = new LinkedBlockingDeque<>();

        CountDownLatch countDownLatch;
        synchronized (servers) {
            countDownLatch = new CountDownLatch(servers.size());
            servers.forEach(server -> executorService.submit(() -> {
                try {
                    query(query, server, res -> {
                        try {
                            queue.put(res);
                        } catch (InterruptedException e) {
                            LOGGER.fine("interrupted, stop to put resource");
                        }
                    });
                } catch (IOException e) {
                    LOGGER.fine("fail to query with " + server);
                    // TODO: should we remove that server?
                } finally {
                    countDownLatch.countDown();
                }
            }));
            try {
                // TODO: add timeout here?
                countDownLatch.await();
            } catch (InterruptedException e) {
                LOGGER.fine("interrupted on countDownLatch");
            }
            consumerThread.interrupt();
        }
        return queue;
    }

    private void query(@NotNull Query query, @NotNull Server server,
                       Consumer<Resource> consumer)
            throws IOException {
        EZInputOutput io = new EZInputOutput(server);
        io.sendJSON(query);
        Response response = io.readResponse();
        if (!response.isSuccess()) {
            LOGGER.fine(String.format("query %s failed: %s", server, response));
            return;
        }
        io.readResources(consumer);
    }

    public void stop() {
        isRunning = false;
    }
}
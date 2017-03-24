package EZShare.server;

import EZShare.entities.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main class for server.
 * Created by xierch on 2017/3/23.
 */
public class ServerDaemon implements ClientCommandHandler {
    private final static Logger LOGGER = Logger.getLogger(ServerDaemon.class.getName());

    private final ServerOptions options;
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
            Client client = null;
            try {
                client = new Client(socket, this);
                client.handle();
            } catch (IOException e) {
                LOGGER.warning("error on handle client " + socket.getInetAddress() + ": " + e);
            } finally {
                if (client != null) {
                    LOGGER.info("disconnect from " + client);
                    client.close();
                }
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

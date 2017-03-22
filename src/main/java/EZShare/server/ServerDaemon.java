package EZShare.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * Main class for server.
 * Created by xierch on 2017/3/23.
 */
public class ServerDaemon {
    private final static Logger LOGGER = Logger.getLogger(ServerDaemon.class.getName());

    private final ServerOptions options;
    private ServerSocket serverSocket;


    public ServerDaemon(ServerOptions options) {
        this.options = options;
    }

    public void start() throws IOException {
        LOGGER.info("bind on port " + options.getPort());
        serverSocket = new ServerSocket(options.getPort());
    }

    public void runForever() throws IOException {
        LOGGER.info("server is running");
    }

    public void stop() {
        LOGGER.info("stopping");
        try {
            serverSocket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}

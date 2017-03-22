package EZShare.server;

/**
 * Store all options for running a server.
 * Created by xierch on 2017/3/22.
 */
public class ServerOptions {
    private final String hostname;
    private final double connectionIntervalLimit;
    private final int exchangeInterval;
    private final int port;
    private final String secret;
    private final boolean debug;

    public ServerOptions(String hostname, double connectionIntervalLimit, int exchangeInterval, int port, String secret,
                         boolean debug) {
        this.hostname = hostname;
        this.connectionIntervalLimit = connectionIntervalLimit;
        this.exchangeInterval = exchangeInterval;
        this.port = port;
        this.secret = secret;
        this.debug = debug;
    }

    public String getHostname() {
        return hostname;
    }

    public double getConnectionIntervalLimit() {
        return connectionIntervalLimit;
    }

    public int getExchangeInterval() {
        return exchangeInterval;
    }

    public int getPort() {
        return port;
    }

    public String getSecret() {
        return secret;
    }

    public boolean isDebug() {
        return debug;
    }
}

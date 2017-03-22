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

    public ServerOptions(String hostname, double connectionIntervalLimit, int exchangeInterval, int port,
                         String secret) {
        this.hostname = hostname;
        this.connectionIntervalLimit = connectionIntervalLimit;
        this.exchangeInterval = exchangeInterval;
        this.port = port;
        this.secret = secret;
    }

    String getHostname() {
        return hostname;
    }

    double getConnectionIntervalLimit() {
        return connectionIntervalLimit;
    }

    int getExchangeInterval() {
        return exchangeInterval;
    }

    int getPort() {
        return port;
    }

    String getSecret() {
        return secret;
    }
}

package EZShare.server;

/**
 * Store all options for running a server.
 * Created on 2017/3/22.
 */
public class ServerOptions {
    private final String hostname;
    private final double connectionIntervalLimit;
    private final int exchangeInterval;
    private final int port;
    private final int sport;
    private final String secret;

    public ServerOptions(String hostname,
                         double connectionIntervalLimit,
                         int exchangeInterval,
                         int port,
                         int sport,
                         String secret) {
        this.hostname = hostname;
        this.connectionIntervalLimit = connectionIntervalLimit;
        this.exchangeInterval = exchangeInterval;
        this.port = port;
        this.sport = sport;
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

    int getSport() {
        return sport;
    }

    String getSecret() {
        return secret;
    }
}

package EZShare.networking;

import EZShare.Server;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

/**
 * Reference: http://stackoverflow.com/a/17352927/1833236
 * Created on 2017/5/13.
 */
public class SecurityHelper {
    static final private Logger LOGGER = Logger.getLogger(Server.class.getName());
    static final private String STORE_FILENAME_CLIENT = "keystore-client";
    static final private String STORE_FILENAME_SERVER = "keystore-server";
    static final private String STORE_FILENAME_TEST = "keystore-debug";
    static final private char[] STORE_PASSWORD = "comp90015".toCharArray();

    private final boolean isClient;

    private SecurityHelper(boolean isClient) {
        this.isClient = isClient;
    }

    public static SecurityHelper getClient() {
        return new SecurityHelper(true);
    }

    public static SecurityHelper getServer() {
        return new SecurityHelper(false);
    }

    private InputStream getStoreInputStream() throws IOException {
        InputStream input = SecurityHelper.class
                .getResourceAsStream(isClient ? STORE_FILENAME_CLIENT : STORE_FILENAME_SERVER);
        if (input == null) {
            input = SecurityHelper.class.getResourceAsStream(STORE_FILENAME_TEST);
            if (input == null)
                throw new IOException("keystore-debug is missing");
            LOGGER.warning("release key store is missing, use debugging one instead");
            LOGGER.warning("secure connection IS INSECURE now, only for test");
        }
        return input;
    }

    private KeyStore getKeyStore() throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream input = getStoreInputStream()) {
            keyStore.load(input, STORE_PASSWORD);
        }
        return keyStore;
    }

    private KeyManager[] getKeyManagers(KeyStore keyStore)
            throws NoSuchAlgorithmException, KeyStoreException,
            IOException, CertificateException, UnrecoverableKeyException {
        KeyManagerFactory keyFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(keyStore, STORE_PASSWORD);
        return keyFactory.getKeyManagers();
    }

    private TrustManager[] getTrustManager(KeyStore trustStore)
            throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }

    public SSLContext getSSLContext() throws SecuritySetupException {
        try {
            KeyStore keyStore = getKeyStore();
            KeyManager keyManagers[] = getKeyManagers(keyStore);
            TrustManager trustManagers[] = getTrustManager(keyStore);
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(keyManagers, trustManagers, null);
            return context;
        } catch (IOException | CertificateException
                | NoSuchAlgorithmException | KeyStoreException
                | UnrecoverableKeyException | KeyManagementException e) {
            throw new SecuritySetupException("fail to setup SSL context", e);
        }
    }
}

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
    static final private String STORE_FILENAME = "keystore";
    static final private String STORE_FILENAME_TEST = "keystore-debug";
    static final private char[] STORE_PASSWORD = "comp90015".toCharArray();

    private static InputStream getStoreInputStream() throws IOException {
        InputStream input = SecurityHelper.class.getResourceAsStream(STORE_FILENAME);
        if (input == null) {
            input = SecurityHelper.class.getResourceAsStream(STORE_FILENAME_TEST);
            if (input == null)
                throw new IOException("keystore-debug is missing");
            LOGGER.warning("release key store is missing, use debugging one instead");
            LOGGER.warning("secure connection IS INSECURE now, only for test");
        }
        return input;
    }

    private static KeyStore getKeyStore() throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream input = getStoreInputStream()) {
            keyStore.load(input, STORE_PASSWORD);
        }
        return keyStore;
    }

    private static KeyManager[] getKeyManagers(KeyStore keyStore)
            throws NoSuchAlgorithmException, KeyStoreException,
            IOException, CertificateException, UnrecoverableKeyException {
        KeyManagerFactory keyFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(keyStore, STORE_PASSWORD);
        return keyFactory.getKeyManagers();
    }

    private static TrustManager[] getTrustManager(KeyStore trustStore)
            throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }

    public static SSLContext getSSLContext() throws SecuritySetupException {
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

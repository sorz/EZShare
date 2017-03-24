package EZShare.networking;

import EZShare.entities.Command;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Basic I/O operations for EZ protocol.
 * Created by xierch on 2017/3/23.
 */
public class EZInputOutput {
    private final static Logger LOGGER = Logger.getLogger(EZInputOutput.class.getName());

    private final ObjectMapper mapper = ObjectMapperGetter.get();

    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public EZInputOutput(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void close() {
        try {
            getSocket().close();
        } catch (IOException e) {
            // ignore
        }
    }

    public Command readCommand() throws IOException {
        String jsonString = getInputStream().readUTF();
        LOGGER.fine("read command:" + jsonString);
        return mapper.readValue(jsonString, Command.class);
    }

    public void sendJSON(Object value) throws IOException {
        String jsonString = mapper.writeValueAsString(value);
        LOGGER.fine("send JSON:" + jsonString);
        getOutputStream().writeUTF(jsonString);
        getOutputStream().flush();
    }

    public void uncheckedSendJSON(Object value) throws UncheckedIOException {
        try {
            sendJSON(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public String toString() {
        return "" + getSocket().getInetAddress();
    }
}

package EZShare.networking;

import EZShare.entities.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
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

    private String readBufferedLine;

    public EZInputOutput(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public EZInputOutput(String hostname, int port) throws IOException {
        this(new Socket(hostname, port));
    }

    public EZInputOutput(Server server) throws IOException {
        this(server.getHostname(), server.getPort());
    }

    public void close() {
        try {
            getSocket().close();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Read a UTF string from socket.
     * @return the String read.
     * @throws IOException if error on read.
     */
    private String readString() throws IOException{
        String string = getInputStream().readUTF();
        LOGGER.fine("read: " + string);
        return string;
    }

    /**
     * Read a JSON value from either read from socket or last fail-to-parsed
     * string in buffer.
     * When JsonParseException or JsonMappingException throws, current string
     * will be kept on buffer for next parsing.
     * @param type same as ObjectMapper.readValue()
     * @param <T> same as ObjectMapper.readValue()
     * @return same as ObjectMapper.readValue()
     * @throws IOException if error on read socket.
     * @throws JsonParseException same as ObjectMapper.readValue()
     * @throws JsonMappingException same as ObjectMapper.readValue()
     */
    public <T> T readJSON(Class<T> type) throws IOException {
        if (readBufferedLine == null)
            readBufferedLine = readString();
        T result = mapper.readValue(readBufferedLine, type);
        readBufferedLine = null;
        return result;
    }

    public Command readCommand() throws IOException {
        return readJSON(Command.class);
    }

    public Response readResponse() throws IOException {
        return readJSON(Response.class);
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

    /**
     * Read zero or more Resource object until ResultSize is read.
     * @param consumer accept read Resource.
     * @throws IOException error on reading or parsing.
     */
    public void readResources(Consumer<Resource> consumer) throws IOException {
        int resourceCount = 0;
        try {
            consumer.accept(readJSON(Resource.class));
            resourceCount ++;
        } catch (JsonMappingException e) {
            ResultSize resultSize = readJSON(ResultSize.class);
            if (resultSize.get() != resourceCount)
                LOGGER.warning(String.format("received %d result(s) but result size is %d",
                        resourceCount, resultSize.get()));
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

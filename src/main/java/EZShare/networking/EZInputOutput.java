package EZShare.networking;

import EZShare.entities.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Basic I/O operations for EZ protocol.
 * Created on 2017/3/23.
 */
public class EZInputOutput {
    private final static Logger LOGGER = Logger.getLogger(EZInputOutput.class.getName());
    private final static int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;  // 30 seconds

    private final ObjectMapper mapper = ObjectMapperGetter.get();
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    private String readBufferedLine;

    public EZInputOutput(Socket socket, int timeout) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(timeout);
        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public EZInputOutput(Socket socket) throws IOException {
        this(socket, DEFAULT_SOCKET_TIMEOUT);
    }

    public EZInputOutput(Server server, int timeout) throws IOException {
        this(new Socket(server.getHostname(), server.getPort()), timeout);
    }

    public EZInputOutput(Server server) throws IOException {
        this(server, DEFAULT_SOCKET_TIMEOUT);
    }


    public void close() {
        IOUtils.closeQuietly(getSocket());
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
     * string in the internal buffer.
     * When {@link JsonParseException} or {@link JsonMappingException} throws,
     * current string will be kept on a internal buffer for next parsing.
     * @param type same as {@link ObjectMapper#readValue(String, Class)}.
     * @param <T> same as {@link ObjectMapper#readValue(String, Class)}.
     * @return same as same as {@link ObjectMapper#readValue(String, Class)}.
     * @throws IOException if error on read socket.
     * @throws JsonParseException same as {@link ObjectMapper#readValue(String, Class)}.
     * @throws JsonMappingException same as {@link ObjectMapper#readValue(String, Class)}.
     */
    public <T> T readJSON(Class<T> type) throws IOException {
        if (readBufferedLine == null)
            readBufferedLine = readString();
        T result = mapper.readValue(readBufferedLine, type);
        readBufferedLine = null;
        return result;
    }

    /**
     * Wrapper for {@link #readJSON(Class)}, to read a {@link Command}.
     * @return the parsed {@link Command}
     * @throws IOException error on network or parsing.
     */
    public Command readCommand() throws IOException {
        return readJSON(Command.class);
    }

    /**
     * Wrapper for {@link #readJSON(Class)}, to read a {@link Response}.
     * @return the parsed {@link Response}
     * @throws IOException error on network or parsing.
     */
    public Response readResponse() throws IOException {
        return readJSON(Response.class);
    }

    /**
     * Serialize object to JSON string and write to network as a Java UTF string.
     * @param value object to write.
     * @throws IOException error on network or JSON serialization.
     */
    public void sendJSON(Object value) throws IOException {
        String jsonString = mapper.writeValueAsString(value);
        LOGGER.fine("send:" + jsonString);
        getOutputStream().writeUTF(jsonString);
        getOutputStream().flush();
    }

    /**
     * Wrapper for {@link #sendJSON(Object)}, but throw {@link UncheckedIOException}
     * rather than normal {@link IOException}. Ideal for use in lambda.
     * @param value the JSON object want send.
     * @throws UncheckedIOException wrapper for a normal {@link IOException}.
     */
    public void uncheckedSendJSON(Object value) throws UncheckedIOException {
        try {
            sendJSON(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read zero or more {@link Response} until {@link ResultSize} is read.
     * @param consumer accept read {@link Response}.
     * @throws IOException error on reading or parsing.
     */
    public int readResources(Consumer<Resource> consumer) throws IOException {
        int resourceCount = 0;
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                consumer.accept(readJSON(Resource.class));
                resourceCount++;
            }
        } catch (JsonMappingException e) {
            ResultSize resultSize = readJSON(ResultSize.class);
            if (resultSize.get() != resourceCount)
                LOGGER.warning(String.format("received %d result(s) but result size is %d",
                        resourceCount, resultSize.get()));
        }
        return resourceCount;
    }

    /**
     * Read binary data and write to output stream.
     * @param outputStream where data write to.
     * @param totalSize total bytes read and write.
     * @return the number of bytes actually read and write.
     * @throws IOException error on read or write.
     */
    public long readBinaryTo(OutputStream outputStream, long totalSize) throws IOException {
        return IOUtils.copyLarge(getInputStream(), outputStream, 0, totalSize);
    }

    /**
     * Write binary data from input stream until EOF.
     * @param inputStream data read from.
     * @throws IOException error on read or write.
     */
    public void writeBinary(InputStream inputStream) throws IOException {
        IOUtils.copyLarge(inputStream, getOutputStream());
    }


    private Socket getSocket() {
        return socket;
    }

    private DataInputStream getInputStream() {
        return inputStream;
    }

    private DataOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public String toString() {
        return "" + getSocket().getInetAddress();
    }
}

package EZShare.server;

import EZShare.entities.*;
import EZShare.networking.EZInputOutput;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

/**
 * Client who connect and send command to server. Other server connect to our
 * is also treated as Client.
 *
 * This class parses command that sent from client and send back response to
 * client. Actual command handing (lookup resource, update resource, etc.) is
 * done on ClientCommandHandler other than this class itself.
 *
 * This class only do a little check on commands. ClientCommandHandler have to
 * check whether a command is well-formatted and legal.
 *
 * Created on 2017/3/23.
 */
class Client implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName());

    final private EZInputOutput io;
    final private ClientCommandHandler commandHandler;
    final private boolean isSecure;

    /**
     * Create a Client.
     * @param socket connected with the client.
     * @param commandHandler to handle client's command.
     * @throws IOException if fail on the socket. Will close by this class.
     */
    Client(Socket socket, ClientCommandHandler commandHandler, boolean isSecure) throws IOException {
        io = new EZInputOutput(socket);
        this.commandHandler = commandHandler;
        this.isSecure = isSecure;
    }

    /**
     * Do everything with the client.
     *
     * This method will block until either command finished or error occurs.
     * ClientCommandHandler may or (may not be, if error occurs) called on the
     * same thread as one invoked this method.
     *
     * Client's socket will be closed before this method return.
     */
    @Override
    public void run() {
        try {
            handleCommand();
        } catch (IOException e) {
            LOGGER.warning("error on handle client " + io + ": " + e);
        } finally {
            io.close();
        }
    }

    /**
     * Just for run(), able to eliminate one block level of try.
     * @throws IOException if error on I/O with client.
     */
    private void handleCommand() throws IOException {
        Command command;
        try {
            command = io.readCommand();
        } catch (JsonMappingException e) {
            LOGGER.info(String.format("Invalid command: [%s] %s", e, e.getMessage()));
            io.sendJSON(Response.createError("invalid command"));
            return;
        } catch (JsonParseException e) {
            LOGGER.info(String.format("Failed to parse command: [%s] %s", e, e.getMessage()));
            io.sendJSON(Response.createError("missing or incorrect type for command"));
            return;
        }
        try {
            switch (command.getCMD()) {
                case PUBLISH:
                    commandHandler.doPublish((Publish) command);
                    io.sendJSON(Response.createSuccess());
                    break;
                case REMOVE:
                    commandHandler.doRemove((Remove) command);
                    io.sendJSON(Response.createSuccess());
                    break;
                case SHARE:
                    commandHandler.doShare((Share) command);
                    io.sendJSON(Response.createSuccess());
                    break;
                case QUERY:
                    int size = commandHandler.doQuery((Query) command,
                            v -> io.uncheckedSendJSON(Response.createSuccess()),
                            io::uncheckedSendJSON);
                    io.sendJSON(new ResultSize(size));
                    break;
                case FETCH:
                    Pair<Resource, InputStream> resInput = commandHandler.doFetch((Fetch) command);
                    io.sendJSON(Response.createSuccess());
                    io.sendJSON(resInput.getLeft());
                    io.writeBinary(resInput.getRight());
                    io.sendJSON(new ResultSize(1));
                    break;
                case EXCHANGE:
                    commandHandler.doExchange((Exchange) command);
                    io.sendJSON(Response.createSuccess());
                    break;
            }
        } catch (CommandHandleException e) {
            LOGGER.info(String.format("Fail to handle command %s: %s",
                    command.getCMD().name(), e.getMessage()));
            io.sendJSON(Response.createError(e.getMessage()));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public String toString() {
        return "" + io;
    }
}

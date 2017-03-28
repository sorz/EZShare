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
 * Client who connect and send command to server.
 * Created by xierch on 2017/3/23.
 */
class Client implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName());

    final private EZInputOutput io;
    final private ClientCommandHandler commandHandler;

    Client(Socket socket, ClientCommandHandler commandHandler) throws IOException {
        io = new EZInputOutput(socket);
        this.commandHandler = commandHandler;
    }

    void close() {
        io.close();
    }

    @Override
    public void run() {
        try {
            handleCommand();
        } catch (IOException e) {
            LOGGER.warning("error on handle client " + io + ": " + e);
        } finally {
            close();
        }
    }

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
                    List<Resource> resources = commandHandler.doQuery((Query) command);
                    io.sendJSON(Response.createSuccess());
                    resources.forEach(io::uncheckedSendJSON);
                    io.sendJSON(new ResultSize(resources.size()));
                    break;
                case FETCH:
                    Pair<Resource, InputStream> resInput = commandHandler.doFetch((Fetch) command);
                    io.sendJSON(Response.createSuccess());
                    io.sendJSON(resInput.getLeft());
                    IOUtils.copy(resInput.getRight(), io.getOutputStream());
                    io.sendJSON(new ResultSize(1));
                    break;
                case EXCHANGE:
                    // TODO
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

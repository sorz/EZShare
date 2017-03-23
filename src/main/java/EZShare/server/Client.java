package EZShare.server;

import EZShare.entities.Command;
import EZShare.networking.EZInputOutput;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Client who connect and send command to server.
 * Created by xierch on 2017/3/23.
 */
class Client {
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName());

    final private EZInputOutput io;

    Client(Socket socket) throws IOException {
        io = new EZInputOutput(socket);
    }

    void close() {
        io.close();
    }

    void handle() throws IOException {
        Command command = io.readCommand();
        LOGGER.info("command " + command);
    }

    @Override
    public String toString() {
        return "" + io;
    }

}

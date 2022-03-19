package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerApp {

    public static final Logger LOGGER_SERVER = LogManager.getLogger();

    public static void main(String[] args) {
        new NanoDropBoxServer().start();
    }

}
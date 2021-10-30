import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientApp {

    public static final Logger LOGGER_CLIENT = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        new NanoDropBoxClient().start();
    }

}

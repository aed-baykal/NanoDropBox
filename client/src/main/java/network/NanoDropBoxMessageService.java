package network;

public interface NanoDropBoxMessageService {
    void send(String msg);
    void receive(String msg);
    void connect();
}

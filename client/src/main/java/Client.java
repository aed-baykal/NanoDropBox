public class Client {

    public static void main(String[] args) throws InterruptedException {
        ClientProcessor clientProcessor = new ClientProcessor();
        // Пока просто, чтобы запускался.
        while (true) {
            clientProcessor.sendMessage();
            Thread.sleep(10000);
        }
    }
}

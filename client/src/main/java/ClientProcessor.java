import common.ChatMessage;
import common.MessageType;
import network.MessageProcessor;
import network.NanoDropBoxMessageService;
import network.NanoDropBoxMessageServiceImpl;

import java.util.ArrayList;

public class ClientProcessor implements MessageProcessor {

    private static final String PUBLIC = "PUBLIC";
    public ArrayList onlineUsers;
    public NanoDropBoxMessageService messageService;
    private String currentName;

    public ClientProcessor (){
        this.messageService = new NanoDropBoxMessageServiceImpl("localhost", 12256, this);
        this.messageService.connect();
        sendAuth();
    }

    public void sendMessage() {
        String text = "Новая передача файла!!!";
        if (text.isEmpty()) return;
        ChatMessage msg = new ChatMessage();
        String adressee = (String) this.onlineUsers.get(1); // Пока затычка
        
            msg.setMessageType(MessageType.PRIVATE);
            msg.setTo(adressee);
        
        msg.setFrom(currentName);
        msg.setBody(text);
        messageService.send(msg.marshall());
        System.out.println("Файл отправлен");
    }

    private void appendTextOfChat(ChatMessage msg) {
        if (msg.getFrom().equals(this.currentName)) return;
        String modifier = msg.getMessageType().equals(MessageType.PUBLIC) ? "[pub]" : "[priv]";
        String text = String.format("[%s] %s %s\n", msg.getFrom(), modifier, msg.getBody());
    }

    private void refreshOnlineUsers(ChatMessage message) {
        message.getOnlineUsers().add(0, PUBLIC);
        this.onlineUsers.add(new ArrayList(message.getOnlineUsers()));
        this.onlineUsers.get(1);
    }

    @Override
    public void processMessage(String msg) {
        new Thread(() -> {
            ChatMessage message = ChatMessage.unmarshall(msg);
            System.out.println("Received message");

            switch (message.getMessageType()) {
                case PRIVATE, PUBLIC -> appendTextOfChat(message);
                case CLIENT_LIST -> refreshOnlineUsers(message);
                case AUTH_CONFIRM -> {
                    this.currentName = message.getBody();
                }
                case CHANGE_USERNAME_CONFIRM -> {
                    currentName = message.getBody();
                }
                case CHANGE_PASSWORD_CONFIRM -> {
                }
                case NEW_USER_CONFIRM -> {
                    currentName = message.getBody();
                }
                case ERROR -> showError(message);
            }
        });
    }

    public void sendAuth() {

        String log = "log1";
        String pass = "pass";
        if (log.isEmpty() || pass.isEmpty()) return;
        ChatMessage msg = new ChatMessage();
        msg.setMessageType(MessageType.SEND_AUTH);
        msg.setLogin(log);
        msg.setPassword(pass);
        messageService.send(msg.marshall());
    }

    private void showError(ChatMessage msg) {
        System.out.println("Что то пошло не так!"); // Пока затычка
    }

}

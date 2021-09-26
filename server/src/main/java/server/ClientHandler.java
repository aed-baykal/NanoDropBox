package server;

import common.ChatMessage;
import common.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    private Socket socket;
    private ChatServer chatServer;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String currentUsername;
    private String currentPassword;
    private boolean exist;
    private static final long TIME_OUT = 3000000;

    public ClientHandler(Socket socket, ChatServer chatServer) {
        try {
            this.chatServer = chatServer;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.exist = true;
            System.out.println("Client handler created!!!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        chatServer.getStartingService().execute(() -> {
            try {
                authenticate();
                if (exist) readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void readMessages() throws IOException {
        try {
            while (!Thread.currentThread().isInterrupted() || socket.isConnected()) {
                String msg = inputStream.readUTF();
                ChatMessage message = ChatMessage.unmarshall(msg);
                ChatMessage responce = new ChatMessage();
                message.setFrom(this.currentUsername);
                //Оставил switch для дальнейшего добавления функционала.
                switch (message.getMessageType()) {
                    case PRIVATE:
                        chatServer.sendPrivateMessage(message);
                        System.out.println("Файл получен!");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeHandler();
        }
    }

    public void sendMessage(ChatMessage message) {
        try {
            outputStream.writeUTF(message.marshall());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentName() {
        return this.currentUsername;
    }

    public String getCurrentPassword() {
        return this.currentPassword;
    }

    private void authenticate() throws SocketException {
        System.out.println("Started client  auth...");
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                    closeHandler();
                    exist = false;
                System.out.println("Connection is closed!");
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, TIME_OUT);
        try {
            while (true) {
                String authMessage = inputStream.readUTF();
                System.out.println("Auth received");
                ChatMessage msg = ChatMessage.unmarshall(authMessage);
                String username = chatServer.getAuthService().getUsernameByLoginAndPassword(msg.getLogin(), msg.getPassword());
                ChatMessage response = new ChatMessage();
                if (username == null || username.equals("")) {
                    response.setMessageType(MessageType.ERROR);
                    response.setBody("Wrong username or password!");
                    System.out.println("Wrong credentials");
                } else if (chatServer.isUserOnline(username)) {
                    response.setMessageType(MessageType.ERROR);
                    response.setBody("Double auth!");
                    System.out.println("Double auth!");
                } else {
                    currentPassword = msg.getPassword();
                    response.setMessageType(MessageType.AUTH_CONFIRM);
                    response.setBody(username);
                    currentUsername = username;
                    chatServer.subscribe(this);
                    System.out.println("Subscribed");
                    timer.cancel();
                    sendMessage(response);
                    break;
                }
                sendMessage(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeHandler() {
        try {
            chatServer.unsubscribe(this);
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
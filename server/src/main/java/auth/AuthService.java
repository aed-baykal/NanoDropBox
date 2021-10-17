package auth;

public interface AuthService {

    void stop();
    String getUsernameByLoginAndPassword(String login, String password);
    String changeUsername(String oldName, String newName);
    String changePassword(String username, String oldPassword, String newPassword);
    User newUser(String UserLogin, String UserPassword);
}

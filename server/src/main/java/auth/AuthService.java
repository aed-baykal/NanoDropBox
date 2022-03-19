package auth;

import java.sql.SQLException;
import java.util.List;
// Интерфейс авторизации
public interface AuthService {
    String getUsernameByLoginAndPassword(String login, String password);
    String changeUsername(String oldName, String newName);
    String changePassword(String username, String oldPassword, String newPassword);
    void newUser(String UserLogin, String UserPassword) throws SQLException;
    String getAllPathsByLogin(String login);
    void setAllPathsByLogin(String login, String allPaths);
    List<User> getUsers();
    void addUser(User user);
    void delUser(String userName);
    Boolean isUserOnline(User user);

}

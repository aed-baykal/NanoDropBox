package auth;

import data_base.ClientsDatabaseService;
import org.apache.logging.log4j.Level;
import server.ServerApp;

import java.util.List;

public class DatabaseAuthService implements AuthService{
    private ClientsDatabaseService dbService;

    public DatabaseAuthService() {
        dbService = ClientsDatabaseService.getInstance();
        ServerApp.LOGGER_SERVER.log(Level.valueOf("Info"), "From DatabaseAuthService - Auth service started");
    }

    @Override
    public String getAllPathsByLogin(String login) {
        return dbService.getAllPathsByLogin(login);
    }

    @Override
    public void setAllPathsByLogin(String login, String allPaths) {
        dbService.setAllPathsByLogin(login, allPaths);
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        return dbService.getUsernameByLoginAndPassword(login, password);
    }

    @Override
    public String changeUsername(String oldName, String newName) {
        return dbService.changeUsername(oldName, newName);
    }

    @Override
    public String changePassword(String username, String oldPassword, String newPassword) {
        return dbService.changePassword(username, oldPassword, newPassword);
    }

    @Override
    public void newUser(String UserLogin, String UserPassword) {
    }

    @Override
    public List<User> getUsers() {
        return dbService.getUsers();
    }

    @Override
    public void addUser(User user) {
        dbService.addUser(user);
    }

    @Override
    public void delUser(String userName) {
        dbService.delUser(userName);
    }

    @Override
    public Boolean isUserOnline(User user) {
        return dbService.isUserOnline(user);
    }
}

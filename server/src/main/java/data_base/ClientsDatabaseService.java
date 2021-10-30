package data_base;

import auth.AuthService;
import auth.User;
import org.apache.logging.log4j.Level;
import server.ServerApp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// Клиент базы данных
public class ClientsDatabaseService implements AuthService {

    private static ClientsDatabaseService instance;
    private static final String CONNECTION = "jdbc:sqlite:/home/andrey/IntellijWorkPlace/NanoDropBox/server/src/main/resources/chat_users.db";
    private static Connection connection;
    private final String GET_USERNAME = "select userlogin from users where userlogin = ? and userpassword = ?;";
    private final String GET_ALLPATHS = "select allpaths from users where userlogin = ?;";
    private final String CHANGE_USERNAME = "update users set userlogin = ? where userlogin = ?;";
    private final String CHANGE_ALLPATHS = "update users set allpaths = ? where userlogin = ?;";
    private final String CHANGE_PASSWORD = "update users set userpassword = ? where userpassword = ? and userlogin = ?;";
    private final String ADD_NEW_USER = "INSERT OR IGNORE INTO users (userlogin, userpassword) VALUES (userlogin = ?, userpassword = ?);";
    private Statement statement;
    private List<User> users = new ArrayList<>();

    @Override
    public String getAllPathsByLogin(String login) {
        try (PreparedStatement ps = connection.prepareStatement(GET_ALLPATHS)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("AllPaths");
        } catch (SQLException e) {
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From ClientsDatabaseService - " + e);
        }
        return "NULL";
    }

    @Override
    public void setAllPathsByLogin(String login, String allPaths) {
        try (PreparedStatement ps = connection.prepareStatement(CHANGE_ALLPATHS)) {
            ps.setString(1, allPaths);
            ps.setString(2, login);
            ps.executeUpdate();
        } catch (SQLException e) {
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From ClientsDatabaseService - " + e);
        }
    }

    public ClientsDatabaseService() {
        try {
            this.statement = connect();
            instance = this;
        } catch (SQLException e) {
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From ClientsDatabaseService - " + e);
        }
    }

    public static ClientsDatabaseService getInstance() {
        if (instance != null) return instance;
        instance = new ClientsDatabaseService();
        return instance;
    }

    static Statement connect() throws SQLException {
        connection = DriverManager.getConnection(CONNECTION);
        return connection.createStatement();
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(GET_USERNAME)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.close();
                return login;
            }
        } catch (SQLException e) {
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From ClientsDatabaseService - " + e);
        }
        return "FALSE";

    }

    @Override
    public String changeUsername(String oldName, String newName) {
        try(PreparedStatement ps = connection.prepareStatement(CHANGE_USERNAME)){
            ps.setString(2, oldName);
            ps.setString(1, newName);
            if (ps.executeUpdate() > 0) return newName;
        } catch (SQLException e) {
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From ClientsDatabaseService - " + e);
        }
        return oldName;
    }

    @Override
    public String changePassword(String username, String oldPassword, String newPassword) {
        try(PreparedStatement ps = connection.prepareStatement(CHANGE_PASSWORD)){
            ps.setString(1, newPassword);
            ps.setString(2,oldPassword);
            ps.setString(3, username);
            if (ps.executeUpdate() > 0) return newPassword;
        } catch (SQLException e) {
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From ClientsDatabaseService - " + e);
        }
        return oldPassword;
    }

    public void newUser(String newUserName, String newPassword) {
        try(PreparedStatement ps = connection.prepareStatement(ADD_NEW_USER)){
            ps.setString(1, newUserName);
            ps.setString(2, newPassword);
            ps.executeUpdate();
        } catch (SQLException e) {
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From ClientsDatabaseService - " + e);
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void delUser(String userName) {
        users.removeIf(user -> user.getLogin().equals(userName));
    }

    public Boolean isUserOnline(User newUser) {
        for (User user : users) {
            if (user.getLogin().equals(newUser.getLogin())) return true;
        }
        return false;
    }

}

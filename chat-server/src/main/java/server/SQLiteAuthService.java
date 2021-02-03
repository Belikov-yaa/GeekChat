package server;

import java.sql.*;

public class SQLiteAuthService implements AuthService {

    private Connection connection;
    private Statement statement;

    public SQLiteAuthService() {
        connect();
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:GeekChat.db");
            statement = connection.createStatement();
            System.out.println("AuthService init ok");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            connection.close();
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try (PreparedStatement prepStatement = connection.prepareStatement("SELECT nickname FROM Users WHERE login=? AND password=?;")) {
            prepStatement.setString(1, login);
            prepStatement.setString(2, password);
            ResultSet resultSet = prepStatement.executeQuery();
            if (resultSet.next()) return resultSet.getString("nickname");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try (PreparedStatement prepInsert = connection.prepareStatement("INSERT INTO Users (login, password, nickname) VALUES (?, ?, ?)")) {
            ResultSet resultSet = statement.executeQuery(String.format("SELECT count() FROM Users WHERE login='%s' or nickname='%s';", login, nickname));
            if (resultSet.next() && resultSet.getInt(1) > 0) return false;
            prepInsert.setString(1, login);
            prepInsert.setString(2, password);
            prepInsert.setString(3, nickname);
            if (prepInsert.executeUpdate()>0) return true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}

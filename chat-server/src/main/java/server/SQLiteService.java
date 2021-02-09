package server;

import java.sql.*;

public class SQLiteService {

    public static Connection connection;

    private static PreparedStatement psGetNickname;
    //    private static PreparedStatement psGetIdNickname;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNickname;

    private static PreparedStatement psGetMessageForNick;
    private static PreparedStatement psAddMessage;


    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:GeekChat.db");
            initPrepStatement();
//            System.out.println("AuthService init ok");
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void initPrepStatement() throws SQLException {
        psRegistration = connection.prepareStatement("INSERT INTO Users (login, password, nickname) VALUES (?, ?, ?)");
//        psGetIdNickname = connection.prepareStatement("SELECT id FROM User WHERE nickname = ?;");
        psGetNickname = connection.prepareStatement("SELECT nickname FROM Users WHERE login=? AND password=?;");
        psChangeNickname = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");

        psGetMessageForNick = connection.prepareStatement("SELECT S.nickname, R.nickname, message FROM Messages " +
                "LEFT JOIN Users S ON Messages.senderID=S.id " +
                "LEFT JOIN Users R ON Messages.receiverID=R.id " +
                "WHERE R.id=(SELECT id FROM Users WHERE nickname = ?) OR R.id IS NULL");

        psAddMessage = connection.prepareStatement("INSERT INTO Messages (senderID, receiverID, message) VALUES (" +
                "(SELECT id FROM Users WHERE nickname = ?), " +
                "(SELECT id FROM Users WHERE nickname = ?), " +
                " ?)");
    }

    public static void disconnect() {
        try {
            psChangeNickname.close();
            psRegistration.close();
            psGetNickname.close();
//            psGetIdNickname.close();
            psAddMessage.close();
            psGetMessageForNick.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static String getNicknameByLoginAndPassword(String login, String password) {
        try {
            psGetNickname.setString(1, login);
            psGetNickname.setString(2, password);
            ResultSet resultSet = psGetNickname.executeQuery();
            if (resultSet.next()) return resultSet.getString("nickname");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean registration(String login, String password, String nickname) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            if (psRegistration.executeUpdate() > 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean changeNick(String oldNick, String newNick) {
        try {
            psChangeNickname.setString(1, newNick);
            psChangeNickname.setString(2, oldNick);
            if (psChangeNickname.executeUpdate() > 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeMessage(String senderNick, String receiverNick, String message) {
        try {
            psAddMessage.setString(1, senderNick);
            psAddMessage.setString(2, receiverNick);
            psAddMessage.setString(3, message);
            if (psAddMessage.executeUpdate() > 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getMessagesFromDB(String nickname) {
        StringBuilder sb = new StringBuilder();
        try {
            psGetMessageForNick.setString(1, nickname);
            ResultSet rs = psGetMessageForNick.executeQuery();
            while (rs.next()) {
                String senderNick = rs.getString(1);
                String receiverNick = rs.getString(2);
                String message = rs.getString("message");
                if (receiverNick == null) {
                    sb.append(String.format("[ %s ]: %s\n", senderNick, message));
                } else {
                    sb.append(String.format("[ %s ] to [ %s ]: %s\n", senderNick, receiverNick, message));
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}

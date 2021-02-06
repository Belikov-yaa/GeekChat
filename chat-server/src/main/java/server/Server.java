package server;

import commands.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private final List<ClientHandler> clients;
    private final AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
//        authService = new SimpleAuthService();
        if (!SQLiteService.connect()) throw new RuntimeException("Не удалось подключиться к БД");
        authService = new DBAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SQLiteService.disconnect();
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler clientHandler, String msg) {
        String message = String.format("[ %s ]: %s", clientHandler.getNickname(), msg);
        if (!SQLiteService.writeMessage(clientHandler.getNickname(),"", msg))
            System.out.println("Сообщение не записалось");
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void broadcastClientList() {
        StringBuilder stringBuilder = new StringBuilder(Command.CLIENT_LIST);
        for (ClientHandler c : clients) {
            stringBuilder.append(" ").append(c.getNickname());
        }
        String msg = stringBuilder.toString();
        clients.forEach(c -> c.sendMsg(msg));
    }

    public void sendPersonalMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] to [ %s ]: %s", sender.getNickname(), receiver, msg);
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(receiver)) {
                c.sendMsg(message);
                if (!SQLiteService.writeMessage(sender.getNickname(), c.getNickname(), msg)) {
                    throw new RuntimeException("Не удалось записать сообщение в БД");
                }
                if (!c.equals(sender)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }
        sender.sendMsg(String.format("User %s not found", receiver));
    }

    void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler client : clients) {
            if (client.getLogin().equals(login))
                return true;
        }
        return false;
    }
}

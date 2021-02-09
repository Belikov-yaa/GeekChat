package server;

public class DBAuthService implements AuthService {
    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        return SQLiteService.getNicknameByLoginAndPassword(login, password);
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        return SQLiteService.registration(login, password, nickname);
    }

    @Override
    public boolean changeNick(String oldNick, String newNick) {
        return SQLiteService.changeNick(oldNick, newNick);
    }
}

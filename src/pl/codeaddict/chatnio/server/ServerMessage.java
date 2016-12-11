package pl.codeaddict.chatnio.server;

/**
 * Created by Michal Kostewicz on 11.11.16.
 */
public enum ServerMessage {
    LOGOUT("User logout"), LOGIN("User login"),  INVALID_COMMAND("Invalid command from client!");

    private String message;

    ServerMessage(String s) {
        this.message = s;
    }

    public String getMessage() {
        return message;
    }
}

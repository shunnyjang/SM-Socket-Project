package ite.smu.socketmanager_app;

public class ChatMessage {
    public boolean left = false;
    public String message = "";

    public ChatMessage(boolean left, String message) {
        super();
        this.left = left;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
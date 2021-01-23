package ite.smu.socketmanager_app;

public class ChatUserItem {
    private User user;
    private String userId;
    private String userNick;

    public ChatUserItem(String userId, String userNick) {
        this.userId = userId;
        this.userNick = userNick;
    }

    public void setUser(User u) {
        user = u;
        setUserId(u.getId());
        setUserNick(u.getNickName());
    }

    public void setUserId(String id) {
        userId = id;
    }
    public void setUserNick(String nick) {
        userNick = nick;
    }

    public User getUser() { return user; }
    public String getUserId() { return userId; }
    public String getUserNick() { return userNick; }
    public String getUserString() { return userNick + "(" + userId + ")"; }

}

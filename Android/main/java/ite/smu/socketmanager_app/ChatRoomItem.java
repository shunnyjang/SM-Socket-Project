package ite.smu.socketmanager_app;

public class ChatRoomItem {
    private String roomNumber;
    private String roomTitle;
    private String roomInfo;

    public ChatRoomItem(String roomInfo) {
        this.roomInfo = roomInfo;
    }

    public ChatRoomItem(String roomNumber, String roomTitle) {
        this.roomNumber = roomNumber;
        this.roomTitle = roomTitle;
        roomInfo = roomNumber + "/" + roomTitle;
    }

    public void setRoomNumber(String num) { roomNumber = num; }
    public void setRoomTitle(String title) { roomTitle = title; }

    public String getRoomInfo() { return roomInfo; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomTitle() { return roomTitle; }
}

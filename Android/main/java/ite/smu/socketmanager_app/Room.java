package ite.smu.socketmanager_app;

import android.app.Activity;

import java.util.ArrayList;

public class Room {

    private String roomNum;
    private String roomName;
    private ArrayList<User> userArray; // 채팅방에 접속한 사람들
    private User maker; // 방장, 방만든사람

    public Room() {
        userArray = new ArrayList<User>();
    }

    public Room(String message) {
        userArray = new ArrayList<User>();
        setRoomName(message);
    }

    public String toProtocol() {
        return roomNum + "/" + roomName;
    }

    // getter/setter
    public String getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(String roomNum) {
        this.roomNum = roomNum;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public ArrayList<User> getUserArray() {
        return userArray;
    }

    public User getMaker() {
        return maker;
    }

    public void setMaker(User user) {
        this.maker = user;
    }

}

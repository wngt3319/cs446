package com.cs446w18.a16.imadog.commands;

import com.cs446w18.a16.imadog.bluetooth.BluetoothServer;
import com.cs446w18.a16.imadog.presenter.PlayerPresenter;
import com.cs446w18.a16.imadog.presenter.UserPresenter;

import java.io.Serializable;

public class CloseNightPollCommand implements Command, Serializable {
    private String name;
    private String role;
    private String winner;
    private String question;
    private UserPresenter receiver;

    public CloseNightPollCommand(String name,  String role, String winner, String question) {
        this.name = name;
        this.role = role;
        this.winner = winner;
        this.question = question;
    }

    public void setReceiver(UserPresenter user) {
        this.receiver = user;
    }
    public void setReceiver(PlayerPresenter player) {}
    public void setReceiver(BluetoothServer server) {}

    public void execute() {
        this.receiver.closeNightPoll(name, role, winner, question);
    }
}

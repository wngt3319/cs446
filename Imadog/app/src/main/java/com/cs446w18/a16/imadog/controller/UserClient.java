package com.cs446w18.a16.imadog.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import com.cs446w18.a16.imadog.activities.GameActivity;
import com.cs446w18.a16.imadog.model.Command;
import com.cs446w18.a16.imadog.model.Room;
import com.cs446w18.a16.imadog.services.ServerThread;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Lacie Yi on 2018-02-25.
 */

public class UserClient {
    private String userName;
    private Room room;
    private GameActivity view;
    private BluetoothSocket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean isHost;
    private UserHost host;
    ServerThread thread;

    public UserClient(String name) {
        userName = name;
        room = null;
        view = null;
        isHost = false;
        this.socket = socket;
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch(Exception e) {

        }
    }

    public void getInput() throws Exception{
        if (!isHost) {
            Command command = (Command) ois.readObject();
            ArrayList<Object> args = command.getArgs();
            switch (command.getCommand()) {
                case "INITIALIZE_GAME":
                    initializeGame();
                    break;
                case "START_DAY_POLL":
                    String question = (String) args.get(0);
                    HashMap<String, String> answers = (HashMap<String, String>) args.get(1);
                    startPoll(question, answers);
                    break;
                case "CLOSE_DAY_POLL":
                    String name = (String) args.get(0);
                    String role = (String) args.get(1);
                    String winner = (String) args.get(2);
                    closePoll(name, role, winner);
                    break;
                case "START_NIGHT_POLL":
                    String title = (String) args.get(0);
                    ArrayList<String> names = (ArrayList<String>) args.get(1);
                    startNightPoll(title, names);
                    break;
                case "CLOSE_NIGHT_POLL":
                    name = (String) args.get(0);
                    role = (String) args.get(1);
                    winner = (String) args.get(2);
                    closeNightPoll(name, role, winner);
                    break;
                case "END_GAME":
                    winner = (String) args.get(0);
                    endGame(winner);
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        userName = name;
    }

    public void joinRoom(Room room) {
        boolean result = room.addMember(this);
        if (result) {
            this.room = room;
        }
    }

    public void leaveRoom() {
        this.room.removeMember(this);
    }

    public void createGame(BluetoothAdapter btAdapter, String MY_UUID) {
        isHost = true;
        room = new Room(btAdapter, MY_UUID);
    }

    public void startGame() {
        host = room.startGame();
    }

    public void initializeGame() {
        view.showDayPage();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                view.showQuestionPage(getQuestion());
                if (isHost) {
                    host.readyToAskQuestion();
                }
            }
        }, 5000);
    }

    public void setView(GameActivity view) {
        this.view = view;
    }

    public void submitAnswer(String answer) throws Exception {
        if (isHost) {
            host.submitAnswer(answer);
        } else {
            ArrayList<Object> args = new ArrayList<>();
            args.add(answer);
            sendCommand("SUBMIT_ANSWER", args);
        }
    }

    public String getQuestion() {
        if (isHost) {
            return host.getQuestion();
        }

        try {
            ArrayList<Object> args = new ArrayList<>();
            sendCommand("GET_QUESTION", args);
            Command rcom = (Command) ois.readObject();
            return (String) rcom.getArgs().get(0);
        } catch (Exception e) {}

        return null;
    }

    public void readyToStart() {
        if (isHost) {
            host.readyToStart();
        }
    }

    public void readyForNight() {
        if (isHost) {
            host.readyForNight();
        } else {
            ArrayList<Object> args = new ArrayList<>();
            sendCommand("READY_FOR_NIGHT", args);
        }
    }

    public void startPoll(String question, HashMap<String, String> answers) {
        view.showVotePage(question, answers);
    }

    public void closePoll(String name, String role, String winner) {
        final String result = winner;
        view.showVictimPage(name, role);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                if (result == null) {
                    view.showNightPage();
                    try {
                        readyForNight();
                    } catch(Exception e) {

                    }
                } else {
                    view.showOutro(result);
                }
            }
        }, 5000);
    }

    public void startNightPoll(String title, ArrayList<String> names) {
        view.showNightVotePage(title, names);
    }

    public void closeNightPoll(String name,  String role, String winner) {
        final String result = winner;
        view.showVictimPage(name, role);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                if (result == null) {
                    initializeGame();
                } else {
                    view.showOutro(result);
                }
            }
        }, 5000);
    }

    public void vote(String choice) {
        if (isHost) {
            host.vote(choice);
        } else {
            ArrayList<Object> args = new ArrayList<>();
            args.add(choice);
            sendCommand("VOTE", args);
        }
    }

    public void endGame(String winner) {
        view.showOutro(winner);
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//
//            public void run() {
//                view.
//            }
//        }, 5000);
    }

    private void sendCommand(String comm, ArrayList<Object> args) {
        try {
            Command c = new Command(comm, args);
            oos.writeObject(c);
        } catch(Exception e) {}
    }
}
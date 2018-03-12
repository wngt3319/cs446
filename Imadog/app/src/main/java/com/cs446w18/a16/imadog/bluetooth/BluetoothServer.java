package com.cs446w18.a16.imadog.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.cs446w18.a16.imadog.commands.Command;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * Created by JayP on 2018-03-11.
 */

public class BluetoothServer extends Bluetooth {
    private BluetoothServerSocket serverSocket;
    private HashMap<Integer, BluetoothSocket> clients;
    int clientCount; // TODO: Need to change to some kind of Player format

    public BluetoothServer(Context context) {
        super(context);
        serverSocket = null;
        clients = new HashMap();
        clientCount = 0;
    }

    public void accept(String roomName, boolean insecureConnection) {
        new AcceptThread(roomName, insecureConnection);
    }

    @Override
    public void send(Command cmd) {
        for (BluetoothSocket client : clients.values()) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                send(cmd, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class AcceptThread extends Thread {
        AcceptThread(String roomName, boolean insecureConnection) {
            try {
                if(insecureConnection) {
                    BluetoothServer.this.serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(roomName, uuid);
                } else {
                        BluetoothServer.this.serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(roomName, uuid);
                }
            } catch (IOException e) {
                if(communicationCallback!=null){
                    communicationCallback.onError(e.getMessage());
                }
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                while (true) {
                    final BluetoothSocket client = serverSocket.accept();
                    clients.put(clientCount++, client);

                    ObjectInputStream input = (ObjectInputStream)client.getInputStream();
                    new ReceiveThread(input);

                    if(communicationCallback!=null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                communicationCallback.onAccept(client);
                            }
                        });
                    }
                }

            } catch (final IOException e) {
                if(communicationCallback!=null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            communicationCallback.onConnectError(device, e.getMessage());
                        }
                    });
                }

                try {
                    socket.close();
                } catch (final IOException closeException) {
                    if (communicationCallback != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                communicationCallback.onError(closeException.getMessage());
                            }
                        });
                    }
                }
            }
        }
    }

    private class ReceiveThread extends Thread implements Runnable{
        private ObjectInputStream input;

        public ReceiveThread(ObjectInputStream input) {
            this.input = input;
        }

        public void run(){
            Command msg;
            try {
                while((msg = (Command)input.readObject()) != null) {
                    if(communicationCallback != null){
                        final Command msgCopy = msg;
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                communicationCallback.onRequest(msgCopy);
                            }
                        });
                    }
                }
            } catch (final IOException e) {
                connected=false;
                if(communicationCallback != null){
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            communicationCallback.onDisconnect(device, e.getMessage());
                        }
                    });
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.example.networking;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived; // callback for received messages

    public Client(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void connect(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("Server: " + msg);
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(msg); // forward to ChessMadness
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMove(String move) {
        if (out != null) out.println(move);
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.example.networking;

import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket serverSocket;
    private ClientHandler whitePlayer;
    private ClientHandler blackPlayer;
    private ClientHandler currentTurnPlayer;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket, this);

            if (whitePlayer == null) {
                whitePlayer = handler;
                handler.send("COLOR_WHITE");
                System.out.println("White player connected.");
            } else if (blackPlayer == null) {
                blackPlayer = handler;
                handler.send("COLOR_BLACK");
                System.out.println("Black player connected.");
                // start game
                currentTurnPlayer = whitePlayer;
                whitePlayer.send("YOUR_TURN");
                System.out.println("Two clients connected. Chess game started!");
            } else {
                handler.send("ERROR: Game full");
                socket.close();
                continue;
            }

            new Thread(handler).start();
        }
    }

    // synchronized to avoid race conditions
    public synchronized void handleMove(String move, ClientHandler sender) {
        if (sender != currentTurnPlayer) {
            sender.send("ERROR: Not your turn");
            return;
        }

        // broadcast move to both clients
        if (whitePlayer != null) whitePlayer.send("MOVE:" + move);
        if (blackPlayer != null) blackPlayer.send("MOVE:" + move);

        // switch turn
        currentTurnPlayer = (currentTurnPlayer == whitePlayer) ? blackPlayer : whitePlayer;
        currentTurnPlayer.send("YOUR_TURN");
    }

    public static void main(String[] args) throws IOException {
        new Server().start(5555);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Server server;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void send(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Received from client: " + msg);
                if (msg.startsWith("MOVE:")) {
                    server.handleMove(msg.substring(5), this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}

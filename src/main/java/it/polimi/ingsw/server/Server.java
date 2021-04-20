package it.polimi.ingsw.server;

import it.polimi.ingsw.controller.ServerController;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.network.GameMessageHandler;
import it.polimi.ingsw.network.setup.Room;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    private final int PORT = 31415;
    private final ServerSocket serverSocket;
    List<ClientConnection> pendingConnections = new ArrayList<>();
    private final ServerController serverController=new ServerController(this);
    private final GameMessageHandler handler = new GameMessageHandler();
    private Map<String , Room> rooms = new HashMap<>();

    public Server() throws IOException {
        this.serverSocket = new ServerSocket(PORT);
    }

    public ServerController getServerController() {
        return serverController;
    }

    public List<ClientConnection> getPendingConnections() {
        return pendingConnections;
    }

    public void addRoom(String roomId, Room room){
        rooms.put(roomId,room);
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public void run() {
        while (true) {
            System.out.println("waiting for client connection...");
            try {
                Socket clientSocket = serverSocket.accept();
                ClientConnection clientConnection = new ClientConnection(clientSocket, this);

                ExecutorService executor = Executors.newCachedThreadPool();

                pendingConnections.add(clientConnection);
                executor.submit(clientConnection);
            } catch (IOException e) {
                System.out.println("Connection error");
            }
        }
    }
}

package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.client.ErrorMessage;
import it.polimi.ingsw.network.client.RoomDetailsMessage;
import it.polimi.ingsw.network.setup.Room;

import it.polimi.ingsw.server.ClientConnection;
import it.polimi.ingsw.server.Server;

import java.util.*;
import java.util.stream.Collectors;

public class ServerController {
    private final Server server;
    private int roomId = 999;

    public ServerController(Server server) {
        this.server = server;
    }

    public void createRoom(boolean privateRoom, String username, int numberOfPlayers, ClientConnection clientConnection){
        List<ClientConnection> clientConnections=server.getPendingConnections();
        clientConnections.remove(clientConnection);


        Room room =  new Room(new Game(), numberOfPlayers, privateRoom, clientConnection);
        room.getGame().addPlayer(username);

        int currentRoomId = getRoomId();
        server.addRoom(currentRoomId,room);

        sendRoomDetails(currentRoomId, room, clientConnection);

    }

    public void addPlayerByRoomId(String username,int roomId, ClientConnection clientConnection){

        if (server.getRooms().get(roomId) == null) {
            clientConnection.sendMessage(new ErrorMessage("room not found."));
            return;
        }
        if (server.getRooms().get(roomId).getGame().getPlayers().stream().anyMatch(player -> player.getUsername().equals(username))) {
            clientConnection.sendMessage(new ErrorMessage("username is taken."));
            return;
        }
        Room room = server.getRooms().get(roomId);
        if (room.isFull()) {
            clientConnection.sendMessage(new ErrorMessage("room is full."));
            return;
        }
        room.getGame().addPlayer(username);
        List<ClientConnection> clientConnections=server.getPendingConnections();
        clientConnections.remove(clientConnection);
        room.addConnection(clientConnection);
        sendRoomDetails(roomId, room, clientConnection);
    }

    public void addPlayerToPublicRoom(int numberOfPlayers, String username, ClientConnection clientConnection){

        List<Room> rooms = new ArrayList<>(server.getRooms().values());

        if (numberOfPlayers == 0) {
            Random r = new Random();
            numberOfPlayers = r.nextInt(4)+1;
        }
        Room room;
        try {
            int finalNumberOfPlayers = numberOfPlayers;
            room = rooms.stream().filter(room1 -> room1.getNumberOfPlayers() == finalNumberOfPlayers && !room1.isPrivate()).findAny().orElseThrow();
        } catch (NoSuchElementException e) {
            createRoom(false, username, numberOfPlayers, clientConnection);
            return;
        }

        if(room.getGame().getPlayers().stream().noneMatch(player -> player.getUsername().equals(username))){
            room.getGame().addPlayer(username);
            List<ClientConnection> clientConnections = server.getPendingConnections();
            clientConnections.remove(clientConnection);
            room.addConnection(clientConnection);

            int currentRoomId = -1;
            for (Map.Entry<Integer, Room> entry: server.getRooms().entrySet())
            {
                if (room.equals(entry.getValue())) {
                    currentRoomId = entry.getKey();
                }
            }
            sendRoomDetails(currentRoomId, room, clientConnection);
        }
    }

    public void sendRoomDetails(int roomId, Room room, ClientConnection clientConnection){
        ArrayList<String> players = new ArrayList<>();
        for (Player player : room.getGame().getPlayers()) {
            players.add(player.getUsername());
        }
        room.sendAll(new RoomDetailsMessage(players, room.getNumberOfPlayers(), roomId));
    }

    private int getRoomId(){
        roomId++;
        if (roomId > 9999)roomId=1000;
        return roomId;
    }
}

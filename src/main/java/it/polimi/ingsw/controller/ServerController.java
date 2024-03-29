package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.GameMessageHandler;
import it.polimi.ingsw.network.client.*;
import it.polimi.ingsw.server.Room;

import it.polimi.ingsw.server.ClientConnection;
import it.polimi.ingsw.server.Server;
import it.polimi.ingsw.utils.GameUtils;

import java.util.*;

/**
 * ServerController class, handles the creation of the rooms for the game.
 * Methods are synchronized to handle multiple requests at the same time.
 */
public class ServerController {
    private final Server server;
    private int roomId = 999;

    /**
     * ServerController class constructor
     */
    public ServerController(Server server) {
        this.server = server;
    }

    /**
     * Creates a room for a game
     * @param privateRoom true if it is a private game
     * @param username of the player creating the room
     * @param numberOfPlayers of the game
     * @param clientConnection of the player creating the room
     */
    public synchronized void createRoom(boolean privateRoom, String username, int numberOfPlayers, ClientConnection clientConnection){
        List<ClientConnection> clientConnections=server.getPendingConnections();
        clientConnections.remove(clientConnection);

        int currentRoomId = getRoomId();
        while (GameUtils.readGame(currentRoomId)!=null)currentRoomId=getRoomId();//finds the first available roomId
        Room room =  new Room(new Game(), numberOfPlayers, privateRoom, clientConnection, currentRoomId);
        room.getGame().addPlayer(username);

        server.addRoom(currentRoomId,room);

        sendRoomDetails(currentRoomId, room);

        if (room.isFull()){
            //if it is a solo game the game can quickly start
            startSoloGame(room);
        }
    }

    /**
     * Adds the player to the game that has the selected roomID
     * @param username of the player to add
     * @param roomId of the game the player wants to join
     * @param clientConnection of the player to add
     */
    public synchronized void addPlayerByRoomId(String username,int roomId, ClientConnection clientConnection){
        if (server.getRooms().get(roomId) == null) {
            //there is no room in the server with this id, but there can be one on the server disk
            Game game = GameUtils.readGame(roomId);
            if(game!=null && game.getPlayerByUsername(username) != null){
                //handling server persistence
                ClassicGameController gameController;
                int playerNumber = game.getPlayers().size() ;
                Room room = new Room(game,playerNumber,true, null, roomId);
                room.setRecreated(true);

                if(game.getPlayers().size()==1){
                    gameController = new SoloGameController(room);
                } else gameController = new ClassicGameController(room);
                room.setGameController(gameController);
                server.addRoom(roomId, room);

                List<ClientConnection> clientConnections=server.getPendingConnections();
                clientConnections.remove(clientConnection);
                room.getConnections().remove(null);
                for (int i = 0; i < room.getNumberOfPlayers(); i++) {
                    //when the room is recreated after the server had  gone down only the connection of the player
                    //recreating the room is different from null
                    if(i!=game.getPlayers().indexOf(game.getPlayerByUsername(username))){
                        room.getConnections().add(i, null);
                    }else {
                        room.getConnections().add(game.getPlayers().indexOf(game.getPlayerByUsername(username)), clientConnection);
                    }
                }

                clientConnection.setGameMessageHandler(new GameMessageHandler(gameController, clientConnection, room));
                for (Player player:
                        game.getPlayers()) {
                    if(!player.getUsername().equals(username))player.setActive(false);
                    else player.setActive(true);
                }

                if (room.getNumberOfPlayers() == game.getActivePlayers().size()){
                    //if this was game was a soloGame the game  can quickly resume
                    room.setCurrentTurn(new Turn(username, room.getGameController().computeNextPossibleMoves(false)));
                    clientConnection.sendMessage(new UpdateAndDisplayGameStateMessage(game));
                    clientConnection.sendMessage(new SelectMoveRequestMessage(room.getCurrentTurn().getMoves()));
                    return;
                }

                ArrayList<String> players = new ArrayList<>();
                for (Player p:
                     game.getActivePlayers()) {
                    players.add(p.getUsername());
                }
                //sets the game so that it can resume when every player is connected
                room.getGameController().computeNextPossibleMoves(false);
                room.setCurrentTurn(new Turn(game.getCurrentPlayer().getUsername(), room.getGameController().computeNextPossibleMoves(false)));
                clientConnection.sendMessage(new RoomDetailsMessage(players, room.getNumberOfPlayers(), roomId));
                return;
            }
            clientConnection.sendMessage(new ErrorMessage("room not found"));
            return;
        }
        Room room = server.getRooms().get(roomId);
        if (!room.isFull()  && room.getGame().getPlayerByUsername(username)!=null && room.getGame().getPlayerByUsername(username).isActive()) {
            //Different players cannot have the same username
            clientConnection.sendMessage(new ErrorMessage("username is taken"));
            return;
        }

        //case room is full
        if (room.isFull() && room.getGame().getPlayerByUsername(username)==null) {
            clientConnection.sendMessage(new ErrorMessage("room is full"));
            return;
        }
        //to handle reconnection
        if(room.isFull() && room.getGame().getPlayerByUsername(username)!=null){
           if(room.getGame().getActivePlayers().stream().noneMatch(player -> player.getUsername().equals(username))){
               //this player exists in the game but is not active
               room.getGame().getPlayerByUsername(username).setActive(true);

               clientConnection.setGameMessageHandler(new GameMessageHandler(room.getGameController(), clientConnection, room));
               //replace his old connection with the new one
               room.getConnections().remove(room.getGame().getActivePlayers().indexOf(room.getGame().getPlayerByUsername(username)));
               room.getConnections().add(room.getGame().getActivePlayers().indexOf(room.getGame().getPlayerByUsername(username)), clientConnection);
               List<ClientConnection> clientConnections=server.getPendingConnections();
               clientConnections.remove(clientConnection);
               if(room.getGame().getPlayerByUsername(username).getLeaderCards().size()<=2){
                   //the disconnection happened when the game was already started
                   clientConnection.getGameMessageHandler().setReady(true);
               }
               if(!clientConnection.getGameMessageHandler().isReady()){
                   //the disconnection happened in initial selections phase
                   clientConnection.getGameMessageHandler().initialSelections();
               }
               else {
                   room.sendAll(new StringMessage(username + " is back in the game!"));
                   room.sendAll(new UpdateGameStateMessage(room.getGame()));
                   if(room.isRecreated() ){
                       //the player is reconnecting after the sever went down
                       if( room.getGame().getActivePlayers().size() == room.getNumberOfPlayers()){
                           //if this was the last player missing everyone is brought to the main board
                           room.sendAll(new UpdateAndDisplayGameStateMessage(room.getGame()));
                           //current player can start his turn
                           room.getConnections().get(room.getGame().getPlayers().indexOf(room.getGame().getCurrentPlayer())).sendMessage(new SelectMoveRequestMessage(room.getCurrentTurn().getMoves()));
                           room.setRecreated(false);
                       }
                    } else clientConnection.sendMessage(new UpdateAndDisplayGameStateMessage(room.getGame()));

                   if(room.getGame().getActivePlayers().size()==1){
                       //if it was a solo game the player can quickly resume the game
                       room.setCurrentTurn(new Turn(username, room.getGameController().computeNextPossibleMoves(false)));
                       room.sendAll(new SelectMoveRequestMessage(room.getCurrentTurn().getMoves()));
                   }
               }

               return;
           }
           else {
               clientConnection.sendMessage(new ErrorMessage("room is full"));
               return;
           }
        }
        //standard case: it is a new game, there is  still place and the username was not already taken
        room.getGame().addPlayer(username);
        List<ClientConnection> clientConnections=server.getPendingConnections();
        clientConnections.remove(clientConnection);
        room.addConnection(clientConnection);
        sendRoomDetails(roomId, room);
        if (room.isFull()){
            startGame(room);
        }
    }

    /**
     * Adds the player to a game that has the selected number of player, if the number  is 0 the player will be added to a  random  game
     * @param numberOfPlayers of the game
     * @param username of the  player to add
     * @param clientConnection of the player to add
     */
    public synchronized void addPlayerToPublicRoom(int numberOfPlayers, String username, ClientConnection clientConnection){

        List<Room> rooms = new ArrayList<>(server.getRooms().values());

        if (numberOfPlayers == 0) {
            //if the user sent 0 as number of players he will be added to a random room
            Random r = new Random();
            numberOfPlayers = r.nextInt(4)+1;
        }
        Room room;
        try {
            int finalNumberOfPlayers = numberOfPlayers;
            room = rooms.stream().filter(room1 -> room1.getNumberOfPlayers() == finalNumberOfPlayers && !room1.isPrivate()).findAny().orElseThrow();
        } catch (NoSuchElementException e) {
            //if there is no room with the specified number of player a new room is created
            createRoom(false, username, numberOfPlayers, clientConnection);
            return;
        }

        if(room.getGame().getActivePlayers().stream().anyMatch(player -> player.getUsername().equals(username))){
            //different players in the same room cannot have the same username
            clientConnection.sendMessage(new ErrorMessage("username is taken"));
            return;
        }

        room.getGame().addPlayer(username);
        List<ClientConnection> clientConnections = server.getPendingConnections();
        clientConnections.remove(clientConnection);
        room.addConnection(clientConnection);

        int currentRoomId = -1;
        for (Map.Entry<Integer, Room> entry: server.getRooms().entrySet())
        {
            //find on the server the id of the newly created room
            if (room.equals(entry.getValue())) {
                currentRoomId = entry.getKey();
            }
        }
        sendRoomDetails(currentRoomId, room);

        if (room.isFull()){
            startGame(room);
        }
    }

    /**
     * Sends the players, number of players and roomID of the game to all players
     * @param roomId of the game
     * @param room of the game
     */
    public synchronized void sendRoomDetails(int roomId, Room room){
        ArrayList<String> players = new ArrayList<>();
        for (Player player : room.getGame().getActivePlayers()) {
            players.add(player.getUsername());
        }
        room.sendAll(new RoomDetailsMessage(players, room.getNumberOfPlayers(), roomId));
    }

    /**
     * Compute the id of the next room, it is an int between 1000 and 9999
     * @return the id of the next room
     */
    private synchronized int getRoomId(){
        roomId++;
        if (roomId > 9999)roomId=1000;
        return roomId;
    }

    /**
     * When the room is full sets the GameMessageHandler to all players and start the game
     * @param room of the game that is starting
     */
    private synchronized void startGame(Room room){
        room.sendAll(new StringMessage("Game is starting!"));
        ClassicGameController classicGameController = new ClassicGameController(room);
        classicGameController.startGame();
        room.setGameController(classicGameController);
        for (ClientConnection client:
             room.getConnections()) {
            client.setGameMessageHandler(new GameMessageHandler(classicGameController, client, room));
            client.getGameMessageHandler().initialSelections();
        }
    }

    /**
     * Starts a solo game
     * @param room of the game that is starting
     */
    private synchronized void startSoloGame(Room room){
        ClientConnection clientConnection = room.getConnections().get(0);
        ClassicGameController soloGameController = new SoloGameController(room);
        room.setGameController(soloGameController);
        clientConnection.setGameMessageHandler(new GameMessageHandler(soloGameController, clientConnection, room));
        soloGameController.startGame();
        clientConnection.getGameMessageHandler().initialSelections();
    }
}

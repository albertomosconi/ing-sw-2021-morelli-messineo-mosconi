package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.Turn;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shared.Resource;
import it.polimi.ingsw.network.client.*;
import it.polimi.ingsw.network.game.*;
import it.polimi.ingsw.server.Room;
import it.polimi.ingsw.server.ClientConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameMessageHandler {

    private final GameController gameController;
    private final ClientConnection clientConnection;
    private final Room room;

    public GameMessageHandler(GameController gameController, ClientConnection clientConnection, Room room) {
        this.gameController = gameController;
        this.clientConnection = clientConnection;
        this.room = room;
        clientConnection.sendMessage(new DropInitialLeaderCardsRequestMessage(room.getPlayerFromConnection(clientConnection).getLeaderCards()));
    }

    public void handle(SelectCardMessage message) {
        System.out.println(message.getNum());
    }

    public void handle(EndTurnMessage message) {
        System.out.println(message.getName());
    }


    public void handle(DropInitialLeaderCardsResponseMessage message){
        gameController.dropInitialLeaderCards(message.getCard1(), message.getCard2(), room.getPlayerFromConnection(clientConnection).getUsername());
        startPlayingIfReady();
    }

    private void startPlayingIfReady() {
        for (Player p : room.getGame().getPlayers()) {
            if (p.getLeaderCards().size() != 2) return;
        }

        room.sendAll(new GameStateMessage(room.getGame()));

        ClientConnection currentPlayer = room.getConnections().get(room.getGame().getPlayers().indexOf(
                room.getGame().getCurrentPlayer()));

        room.setCurrentTurn(new Turn(room.getPlayerFromConnection(currentPlayer).getUsername(),new ArrayList<String>(){{
            add("DROP_LEADER");
            add("GET_MARBLES");
        }}));

        SelectMoveRequestMessage selectMoveRequestMessage = new SelectMoveRequestMessage(room.getCurrentTurn().getMoves());
        currentPlayer.sendMessage(selectMoveRequestMessage);
    }

    public void handle(SelectMoveResponseMessage message){
        if(room.getCurrentTurn().isValidMove(message.getMove(), room.getPlayerFromConnection(clientConnection).getUsername())){
            switch (message.getMove()){
                case("GET_MARBLES"):
                    clientConnection.sendMessage(new SelectMarblesRequestMessage(room.getGame().getMarket().getMarbleStructure()));
                    break;
                case("DROP_LEADER"):
                    clientConnection.sendMessage(new DropLeaderCardRequestMessage(room.getPlayerFromConnection(clientConnection).getLeaderCards()));
                    break;
                case("SWITCH_SHELVES"):

                    break;
            }

        } else {
            clientConnection.sendMessage(new ErrorMessage("Invalid Move\nRetry"));
            clientConnection.sendMessage(new SelectMoveRequestMessage(room.getCurrentTurn().getMoves()));
        }
    }

    public void handle(SelectMarblesResponseMessage message){
        Map<String, List<Resource>> resources = gameController.getMarbles(message.getRowOrColumn(), message.getIndex());

        room.getCurrentTurn().setNonWhiteMarbles(resources.get("notWhite"));
        room.getCurrentTurn().setWhiteMarbles(resources.get("white"));

        if (resources.get("white").size() > 0 && resources.get("white").get(0).equals(Resource.ANY)) {
            System.out.println(resources.get("white").size());
            System.out.println(resources.get("conversionOptions"));
            return;
        }

        askToDropResources(new ArrayList<>() {{
            addAll(room.getCurrentTurn().getNonWhiteMarbles());
            addAll(room.getCurrentTurn().getWhiteMarbles());
        }});
    }

    private void askToDropResources(List<Resource> resources) {
        clientConnection.sendMessage(new DropResourceRequestMessage(resources));
    }

    public void handle(DropLeaderCardResponseMessage message){
        gameController.dropLeader(message.getCard());
        clientConnection.sendMessage(new StringMessage("Your have " +room.getPlayerFromConnection(clientConnection).getFaithTrack().getPosition() +" faith points"));
    }

    public void handle(SwitchShelvesResponseMessage message){
        gameController.switchShelves(message.getShelf1(), message.getShelf2());
        clientConnection.sendMessage(new StringMessage("Your updated warehouse\n" + room.getPlayerFromConnection(clientConnection).getPlayerBoard().getWarehouse().getShelf("top") +
                room.getPlayerFromConnection(clientConnection).getPlayerBoard().getWarehouse().getShelf("middle") + room.getPlayerFromConnection(clientConnection).getPlayerBoard().getWarehouse().getShelf("bottom") + room.getPlayerFromConnection(clientConnection).getPlayerBoard().getWarehouse().getShelf("extra")));
    }

    public void handle(SelectResourceForWhiteMarbleResponseMessage message) {

    }
}

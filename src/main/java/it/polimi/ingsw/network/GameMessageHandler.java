package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.game.*;
import it.polimi.ingsw.server.ClientConnection;

public class GameMessageHandler {

    private final GameController gameController;
    private final ClientConnection clientConnection;

    public GameMessageHandler(GameController gameController, ClientConnection clientConnection) {
        this.gameController = gameController;
        this.clientConnection = clientConnection;
    }

    public void handle(SelectCardMessage message) {
        System.out.println(message.getNum());
    }

    public void handle(EndTurnMessage message) {
        System.out.println(message.getName());
    }

    public void handle(SelectMarblesMessage message) {
//        For DEBUG
        System.out.println("SelectMarblesMessage");
    }

    public void handle(DropLeaderCardsResponseMessage message){
        gameController.dropInitialLeaderCards(message.getCard1(), message.getCard2(), clientConnection);
    }
}

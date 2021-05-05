package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.market.MarbleStructure;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Shelf;
import it.polimi.ingsw.model.shared.DevelopmentCard;
import it.polimi.ingsw.model.shared.LeaderCard;
import it.polimi.ingsw.model.shared.ProductionPower;
import it.polimi.ingsw.model.shared.Resource;
import it.polimi.ingsw.network.client.StringMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public interface UI {

    void displayRoomDetails(ArrayList<String> players, int playersNum, int RoomId);

    void setup();

    void displayError(String body);

    void displayString(String body);

    void selectLeaderCards(ArrayList<LeaderCard> leaderCards);

    void displayGameState();

    void displayPossibleMoves(List<String> moves);

    void setGameState(Game game);

    void displayMarbles(MarbleStructure marbleStructure);

    void dropResources(List<Resource> resources);

    void discardLeaderCard(ArrayList<LeaderCard> cards);

    void switchShelves(ArrayList<Shelf> shelves);

    void activateProduction(List<ProductionPower> productionPowers);

    void buyDevelopmentCard(List<DevelopmentCard> developmentCards);

    void selectStackToPlaceCard(List<DevelopmentCard> stacks);

    void playLeader(List<LeaderCard> leaderCards);

}

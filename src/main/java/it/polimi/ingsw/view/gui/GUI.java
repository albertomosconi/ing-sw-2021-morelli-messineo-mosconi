package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.LocalClient;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.market.MarbleStructure;
import it.polimi.ingsw.model.shared.DevelopmentCard;
import it.polimi.ingsw.model.shared.LeaderCard;
import it.polimi.ingsw.model.shared.ProductionPower;
import it.polimi.ingsw.model.shared.Resource;
import it.polimi.ingsw.view.UI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class GUI extends Application implements UI {
    private Stage stage;
    private final Map<String, Scene> sceneMap = new HashMap<>();
    private final Map<String, SceneController> controllerMap = new HashMap<>();
    private Client client;

    @Override
    public void start(Stage stage) throws Exception {
        loadScenes();
        this.stage = stage;
        this.stage.setTitle("Masters of Renaissance");
        setScene("online-offline");
    }

    public void initializeClient(boolean online) {
        if (online) {
            this.client = new Client(this);
        } else {
            this.client = new LocalClient(this);
        }
    }

    public Client getClient() {
        return client;
    }

    @Override
    public void run() {
        launch();
    }

    public void setScene(String sceneName) {
        System.out.println("display " + sceneName);
        try {
            Platform.runLater(() -> {
                if (stage.getScene() == null || !stage.getScene().equals(sceneMap.get(sceneMap))) {
                    System.out.println("scene set");
                } else {
                    System.out.println("scene already set");
                }
                stage.setScene(sceneMap.get(sceneName));
                stage.show();
                System.out.println("stage shown");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setup() {
        launch();
    }

    private void loadScenes() {
        for (String sceneName : Arrays.asList("online-offline", "connect", "select-game", "room-details")) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("scenes/" + sceneName +".fxml"));
            try {
                sceneMap.put(sceneName, new Scene(loader.load()));
                SceneController controller = loader.getController();
                controllerMap.put(sceneName, controller);
                controller.setGUI(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void displayRoomDetails(ArrayList<String> players, int playersNum, int RoomId) {
        ((RoomDetailsController)controllerMap.get("room-details")).displayDetails(players, playersNum, RoomId);
        setScene("room-details");
    }

    @Override
    public void displayError(String body) {

    }

    @Override
    public void displayString(String body) {

    }

    @Override
    public void selectInitialResources(List<Resource> resources, int amount) {

    }

    @Override
    public void selectLeaderCards(ArrayList<LeaderCard> leaderCards) {

    }

    @Override
    public void displayGameState() {

    }

    @Override
    public void displayPossibleMoves(List<String> moves) {

    }

    @Override
    public void setGameState(Game game) {

    }

    @Override
    public void selectMarbles(MarbleStructure marbleStructure) {

    }

    @Override
    public void dropResources(Map<Resource, Integer> resources) {

    }

    @Override
    public void discardLeaderCard(ArrayList<LeaderCard> cards) {

    }

    @Override
    public void switchShelves(ArrayList<String> shelves) {

    }

    @Override
    public void activateProduction(List<ProductionPower> productionPowers) {

    }

    @Override
    public void buyDevelopmentCard(List<DevelopmentCard> developmentCards) {

    }

    @Override
    public void selectStackToPlaceCard(List<Integer> stacks) {

    }

    @Override
    public void playLeader(List<LeaderCard> leaderCards) {

    }

    @Override
    public void gameOver(String winner, Map<String, Integer> standing){

    }

    @Override
    public void askUsername() {

    }

    @Override
    public String getUsername() {
        return null;
    }
}

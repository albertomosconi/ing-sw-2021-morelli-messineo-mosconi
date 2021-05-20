package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.player.Player;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class GameViewController implements SceneController {
    @FXML
    private VBox leftContainer;
    @FXML
    private TabPane tabPane;

    public void load(Game game) {
        FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource("scenes/cards-grid.fxml"));
        GridPane cardsGrid;
        try {
            cardsGrid = loader.load();
            ((CardsGridController) loader.getController()).setCards(game.getMarket().getCardsGrid());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // add cards grid
        cardsGrid.setScaleX(0.6);
        cardsGrid.setScaleY(0.6);
        leftContainer.getChildren().add(cardsGrid);
        // populate player tabs
        tabPane.getTabs().clear();
        for (Player p : game.getPlayers()) {
            Tab playerTab = new Tab();
            playerTab.setText(p.getUsername() + ": " + p.getVP() + " points");
            tabPane.getTabs().add(playerTab);
        }
    }

    @Override
    public void setGUI(GUI gui) {

    }
}

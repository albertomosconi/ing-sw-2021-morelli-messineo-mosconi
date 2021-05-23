package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.market.MarketCardStack;
import it.polimi.ingsw.model.shared.DevelopmentCard;
import it.polimi.ingsw.network.game.SelectMoveResponseMessage;
import it.polimi.ingsw.utils.GameUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class CardsMarketController implements SceneController {
    private GUI gui;
    @FXML
    private VBox vbox;

    public void load(List<MarketCardStack> cardStacks) {
        FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource("scenes/cards-grid.fxml"));
        GridPane cardsGrid;
        try {
            cardsGrid = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ((CardsGridController) loader.getController()).setCards(cardStacks);
        cardsGrid.setScaleX(1.9);
        cardsGrid.setScaleY(1.9);
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.setSpacing(160);
        vbox.setPadding(new Insets(0, 0, 30, 0));
        vbox.getChildren().add(0, cardsGrid);
    }

    @FXML
    void cancel(ActionEvent event) {
        vbox.getChildren().remove(0);
        gui.setScene("game-board");
    }

    @Override
    public void setGUI(GUI gui) {
        this.gui = gui;
    }

    public void allowBuy(List<DevelopmentCard> developmentCards) {
        Platform.runLater(() -> {
            vbox.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("scenes/cards-grid.fxml"));
            GridPane cardsGrid;
            try {
                cardsGrid = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            cardsGrid.getChildren().clear();
            cardsGrid.setScaleX(1.9);
            cardsGrid.setScaleY(1.9);
            vbox.setAlignment(Pos.BOTTOM_CENTER);
            vbox.setSpacing(160);
            vbox.setPadding(new Insets(0, 0, 30, 0));
            vbox.getChildren().add(cardsGrid);
            try {

                for (DevelopmentCard card : developmentCards) {
                    ;
                    javafx.scene.image.Image cardImage = new Image(new FileInputStream(
                            "src/main/resources/images/development/development_" +
                                    card.getCardType().name().toLowerCase() + "_" + card.getScore() + ".png"));
                    javafx.scene.image.ImageView cardImageView = new ImageView(cardImage);
                    cardImageView.setPreserveRatio(true);
                    cardImageView.setFitWidth(200);
                    cardImageView.setFitHeight(95);
                    GridPane.setValignment(cardImageView, VPos.CENTER);
                    GridPane.setHalignment(cardImageView, HPos.CENTER);
                    int cardX = (developmentCards.indexOf(card)) / 4;
                    int cardY = (developmentCards.indexOf(card) % 4);
                    cardsGrid.add(cardImageView, cardY, cardX);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }


    public void buyCard(ActionEvent actionEvent) {
        gui.getClient().sendMessage(new SelectMoveResponseMessage("BUY_CARD"));
    }
}
package xuexianchen.crazy8;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.control.TextArea;
import javafx.scene.input.ScrollEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class FXMLController implements Initializable {
    private Dealer dealer;

    @FXML private HBox intro_menu;
    @FXML private VBox game_board;

    @FXML private HBox opponent_deck;
    @FXML private HBox player_deck;

    @FXML private ImageView discard_pile;
    @FXML private HBox suit_selector;
    @FXML private ImageView suit_identifier;

    @FXML private Label notification_panel_label;

    @FXML
    protected void onIntroMenuExit() {
        Platform.exit();
    }

    /**
     * switch UI panel + deal cards, etc.
     */
    @FXML
    protected void onIntroMenuStart(){
        // deal cards
        dealer = new Dealer();

        player_deck.getChildren().clear();
        // initialize player's cards
        var playersCards = dealer.getPlayersCards();
        for (int i = 0; i < playersCards.size(); i++) {
            Card card = playersCards.get(i);

            ImageView imageView = new ImageView(card.getImage());
            imageView.setFitWidth(70);
            imageView.setPreserveRatio(true);
            player_deck.getChildren().add(imageView);

            //add event listener
            imageView.setOnMouseClicked(event -> {
                onPlayerCardClicked(event, card, imageView);
            });
        }

        opponent_deck.getChildren().clear();
        // initialize opponent's cards
        var npCsCards = dealer.getNPCsCards();
        for (int i = 0; i < npCsCards.size(); i++) {
            Card card = npCsCards.get(i);

            /* the below code will show the card front side */
            // ImageView imageView = new ImageView(card.getImage());

            // show card back side
            ImageView imageView = new ImageView(dealer.image_card_back);
            imageView.setFitWidth(70);
            imageView.setPreserveRatio(true);
            opponent_deck.getChildren().add(imageView);
            card.setImageView(imageView);
        }

        // set first discarded card
        onDiscardACard();

        dealer.latestCardProperty().addListener(new ChangeListener<Card>() {
            @Override
            public void changed(ObservableValue<? extends Card> observable, Card oldValue, Card newValue) {
                onDiscardACard();
            }
        });

        suit_identifier.setImage(null);
        dealer.selectedSuitProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue == null){
                    suit_identifier.setImage(null);
                    return;
                }
                var name = "images/widgets/" + newValue + ".png";
                suit_identifier.setImage(new Image(Card.class.getResource(name).toExternalForm()));
            }
        });

        notification_panel_label.textProperty().bind(dealer.notificationProperty());

        // load game board UI
        loadGameBoardUI();
    }

    private void onPlayerCardClicked(MouseEvent event, Card card, ImageView imageView){
        if(suit_selector.isVisible()){
            dealer.setNotificationSuit("You can't play cards now, please select a suit first.");
            return;
        }
        if(!dealer.IsGameOn()){
            dealer.setNotificationSuit("Game is over.");
            return;
        }

        if(dealer.isCardPlayable(card)){
            dealer.playerPlaysACard(card);

            if(card.getRank() == "8" && dealer.getPlayersCards().size() > 0){
                showSuitSelector( );
                dealer.setNotificationSuit("You played a crazy 8, please select a suit.");
            }
            player_deck.getChildren().remove(imageView);
            imageView = null; // release resources
        }
        else {
            dealer.setNotificationSuit("Card (" + card.toString() + ") does not match the previous card.");
        }
    }

    private void onDiscardACard(){
        discard_pile.setImage(dealer.getlatestDiscardedCard().getImage());
    }

    @FXML
    protected void onGameBoardExit(){
        // clear decks

        // switch UI
        loadIntroMenuUI();
    }

    @FXML
    protected void onDrawPileClicked(){
        if(!dealer.IsGameOn()){
            return;
        }

        if(dealer.canPlayerDrawACard()){
            dealer.notificationProperty().setValue("");

            // player draw a card
            Card card = dealer.playerDrawACard();

            // add a new ImageView to player's hand
            ImageView imageView = new ImageView(card.getImage());
            imageView.setFitWidth(70);
            imageView.setPreserveRatio(true);
            player_deck.getChildren().add(imageView);
            imageView.setOnMouseClicked(event -> {
                onPlayerCardClicked(event, card, imageView);
            });

            // Once player draws a card, it's NPC's turn
            dealer.setAsNPCsTurn();
        }
    }

    private void loadIntroMenuUI(){
        intro_menu.setManaged(true);
        intro_menu.setVisible(true);

        game_board.setManaged(false);
        game_board.setVisible(false);
        setGameDescription();
    }

    private void loadGameBoardUI(){
        intro_menu.setManaged(false);
        intro_menu.setVisible(false);
        game_board.setManaged(true);
        game_board.setVisible(true);

        suit_selector.setVisible(false);
    }

    protected void showSuitSelector( ){
        for (var childNode:suit_selector.getChildren()) {
            String childNodeID = childNode.getId();
            if(childNodeID.startsWith("suit_selector")){
                childNode.setOnMouseClicked(event -> {
                    dealer.selectSuit(childNodeID.replaceAll("^suit_selector_", ""), true);
                    suit_selector.setVisible(false);
                    dealer.notificationProperty().setValue("");
                });
            }
            else {
                childNode.setVisible(false);
                childNode.setManaged(false);
            }
        }
        suit_selector.setVisible(true);
    }

    @FXML
    private TextArea game_description_text;
    private void setGameDescription(){
        game_description_text.getStyleClass().add("text-area-no-focus");
        game_description_text.setText("Welcome to Crazy Eight, a classic card game with some exciting twists! In this game, you'll be challenging the computer.\n" +
                "\n" +
                "Objective: Be the first to discard all your cards.\n" +
                "\n" +
                "Starting Hands: Seven cards dealt to each player.\n" +
                "\n" +
                "Gameplay:\n" +
                "-  Discard by matching rank or suit with the top discard pile card.\n" +
                "-  Play \"8\" card anytime to choose the next suit.\n" +
                "-  If unable to play, draw from the draw pile.");
        game_description_text.addEventFilter(ScrollEvent.ANY, ScrollEvent::consume); // Disable scrolling
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadIntroMenuUI();
    }
}
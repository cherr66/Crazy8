package xuexianchen.crazy8;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Dealer {
    private Deck deck;
    private ArrayList<Card> discardPile;

    private ArrayList<Card> playersCards;
    private ArrayList<Card> NPCsCards;
    private int initialCardCount = 7;
    protected Image image_card_back;


    /**
     *  Observable property: selectedSuit;
     */
    private StringProperty selectedSuit = new SimpleStringProperty();
    public StringProperty selectedSuitProperty(){ return selectedSuit; }

    /**
     * Observable property: latestCard
     */
    private ObjectProperty<Card> latestCard = new SimpleObjectProperty<>();
    public ObjectProperty<Card> latestCardProperty() {
        return latestCard;
    }
    public Card getlatestDiscardedCard() {
        return latestCard.get();
    }

    /**
     * Observable String property: notification
     */
    private StringProperty notification = new SimpleStringProperty();
    public StringProperty notificationProperty(){ return notification; }
    public void setNotificationSuit(String value){
        notification.set(value);
    }


    private boolean isPlayerTurn; // the player starts turn, by default = true;

    public Dealer() {
        image_card_back = new Image(Card.class.getResource("images/cards/card_back.png").toExternalForm());

        deck = new Deck(); //draw pile
        deck.shuffle();

        playersCards = dealCards(initialCardCount);
        NPCsCards = dealCards(initialCardCount);

        discardPile = new ArrayList<Card>();
        Card firstCard = deck.dealTopCard();

        // If 8 is the first card, notify the player that any card can be played
        if(firstCard.getRank().equals("8")){
            setNotificationSuit("The first card is 8, you can play any card now.");
        }
        discardPile.add(firstCard);
        latestCard.set(firstCard);

        isPlayerTurn = true;
    }


    /**
     * Drawing multiple cards, call deck.dealTopCard() if only 1 card needed
     * @param count should > 1
     * @return
     */
    private ArrayList<Card> dealCards(int count){
        if(count <= 1 ){
            return null;
        }

        ArrayList<Card> cards = new ArrayList<>();
        int i = 0;
        Card c;
        do{
            c = deck.dealTopCard();
            if(c == null){
                deck.restock(discardPile);
                deck.shuffle();
            }
            else {
                cards.add(c);
                i++;
            }
        }
        while(i < count);
        return cards;
    }

    public ArrayList<Card> getPlayersCards(){
        return playersCards;
    }

    public ArrayList<Card> getNPCsCards(){
        return NPCsCards;
    }


    /**
     * check if a card is playable
     * @return true - playable, false - not playable
      */
    protected boolean isCardPlayable(Card card){
        return isCardPlayable(card.getRank(), card.getSuit());
    }

    protected boolean isCardPlayable(String rank, String suit){
        String lastSuit = latestCard.getValue().getSuit();
        String lastRank = latestCard.getValue().getRank();

        // if the to-play card is 8, return true
        if(rank.equals("8")){
            return true;
        }

        //if the latest played card is 8, check player selected suit
        if(lastRank.equals("8")){
            if(selectedSuit.getValue() == null){
                // this happens when 8 is the first card
                return true;
            }else {
                return (selectedSuit.getValue().equals(suit))? true:false;
            }
        }
        // else, check suit and rank separately
        if(lastSuit.equals(suit) || lastRank.equals(rank)){
            return true;
        }
        return false;
    }


    protected void setAsNPCsTurn(){
        isPlayerTurn = false;
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(actionEvent -> {
            npcPlaysACard();
        });
        pause.play();
    }

    private void npcPlaysACard(){
        ArrayList<Card> playableCards = new ArrayList<>();
        for (Card c:NPCsCards) {
            if(isCardPlayable(c)){
                playableCards.add(c);
            }
        }

        // Get parent node in fxml
        HBox parentNode = (HBox)NPCsCards.get(0).getImageView().getParent();

        if(playableCards.size() > 0){
            // clear selected suit info
            selectedSuit.setValue(null);

            // NPC randomly plays a card (save 8 for later)
            Card card = playableCards.get(0);
            Iterator<Card> iterator = playableCards.iterator();
            while(iterator.hasNext()){
                Card c = iterator.next();
                if(c.getRank().equals("8")){
                    iterator.remove();
                }
            }
            if(playableCards.size() > 0){
                Random random = new Random();
                card = playableCards.get(random.nextInt(playableCards.size()));
            }
            NPCsCards.remove(card);

            if(card.getRank().equals("8")){
                selectedSuit.setValue(NPCsCards.get(0).getSuit());
            }

            ImageView imageView = card.getImageView();
            parentNode.getChildren().remove(imageView);
            imageView = null;
            card.setImageView(null);

            discardPile.add(card);
            latestCard.set(card);
        }else{
            // if no playable cards, NPC draw a card
            Card card = deck.dealTopCard();
            NPCsCards.add(card);

            // the below code will add card front side
            // ImageView imageView = new ImageView(card.getImage());

            // present card back side
            ImageView imageView = new ImageView(image_card_back);
            imageView.setFitWidth(70);
            imageView.setPreserveRatio(true);
            parentNode.getChildren().add(imageView);
            card.setImageView(imageView);
        }

        // check if NPC wins the game
        if(NPCsCards.size() == 0){
            setNotificationSuit("Oops, You lose. Game is over.");
            return;
        }

        isPlayerTurn = true;
    }

    protected void selectSuit(String suit, boolean isPlayerSelect){
        if(!deck.isSuitValid(suit)){
            return;
        }

        selectedSuit.setValue(suit);
        if(isPlayerSelect){
            setAsNPCsTurn();
        }
    }


    /**
     * Check if the player can draw a card at current turn
     * @return true/false
     */
    protected boolean canPlayerDrawACard(){
        if(isPlayerTurn == false){
            setNotificationSuit("It's not your turn.");
            return false;
        }

        // If the player has playable cards, s/he can NOT draw a new card
        for (Card card:playersCards) {
            if(isCardPlayable(card)){
                setNotificationSuit("You have playable cards, you can NOT draw a card now.");
                return false;
            }
        }
        return true;
    }

    protected Card playerDrawACard(){
        isPlayerTurn = false;
        Card card = deck.dealTopCard();
        playersCards.add(card);
        return card;
    }

    /**
     * called when player plays a card
     */
    protected void playerPlaysACard(Card card){
        if(playersCards.contains(card) == false){
            return;
        }
        if(isPlayerTurn == false){
            return;
        }

        // clear selected suit info & notification info
        selectedSuit.setValue(null);
        notificationProperty().setValue("");

        discardPile.add(card);
        latestCard.set(card);
        playersCards.remove(card);

        // check if no cards left, notification UI
        if(playersCards.size() == 0){
            setNotificationSuit("Wow! You win the game.");
            return;
        }

        if(card.getRank().equals("8") == false){
            // otherwise, wait until a new suit is selected
            setAsNPCsTurn();
        }
    }

    protected boolean IsGameOn(){
        if(playersCards.size() <=0 || NPCsCards.size() <= 0){
            return false;
        }
        return true;
    }
}

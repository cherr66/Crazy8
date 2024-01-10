package xuexianchen.crazy8;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Card {
    private String suit;
    private String rank;
    private Image image;
    private ImageView imageView;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
        String fileName = rank + "_of_"+suit+".png";
        image = new Image(Card.class.getResource("images/cards/" + fileName).toExternalForm());
    }


    /**
     * Getters and setters for suit, rank, and Image
     * @return
     */
    public String getRank(){
        return rank;
    }
    public String getSuit(){
        return suit;
    }
    public Image getImage(){
        return image;
    }
    protected ImageView getImageView(){return imageView;}

    protected void setImageView(ImageView imageView){ this.imageView = imageView;}

    @Override
    public String toString() {
        return rank + " of " + suit;
    }

}


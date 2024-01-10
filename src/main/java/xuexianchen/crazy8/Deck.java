package xuexianchen.crazy8;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Card> cards;
    private String[] validSuits = {"hearts", "diamonds", "clubs", "spades"};
    private String[] validRanks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king", "ace"};

    public Deck() {
        cards = new ArrayList<>();

        for (String suit : validSuits) {
            for (String rank : validRanks) {
                Card card = new Card(suit, rank);
                cards.add(card);
            }
        }
    }

    public ArrayList<Card> getDeck() {
        return cards;
    }

    public void shuffle()
    {
        Collections.shuffle(cards);
    }

    public void  restock(ArrayList<Card> stock){
        cards.addAll(stock);
    }

    public Card dealTopCard()
    {
        if (cards.size()>0)
            return cards.remove(0);
        else
            return null;
    }

    public boolean isSuitValid(String s){
        for (String suit : validSuits) {
            if (suit.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
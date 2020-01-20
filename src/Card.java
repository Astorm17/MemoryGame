import javax.swing.JButton;
import javax.swing.ImageIcon;

public class Card extends JButton {

    private boolean match = false;
    private boolean selected = false;


    public static final String DEFAULT_CARD = "images/PlayerBackOfCard.jpg";


    public Card() {
        super(new ImageIcon(DEFAULT_CARD));
    }

    /**
     * Returns if the card is part of a matching pair or not
     * @return if the card is in a matching pair
     */
    public boolean isMatch() {
        return match;
    }

    /**
     * Returns the current state if the card is selected or not
     * @return the state of the card
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * sets the cards state to a matched state
     */
    public void setMatch() {
        match = true;
    }

    /**
     * Determines whether the card has been selected or not
     * @param b
     */
    public void select(boolean b) {
        selected = b;
    }
}


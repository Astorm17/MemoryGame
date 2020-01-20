import java.net.*;
import java.io.*;
import java.util.Random;

/**
 * The service used to play the memory game
 */
public class MemoryGameService implements Runnable, MemoryGameConstants {

    private Socket[] players;

    private DataInputStream[] fromPlayer;
    private DataOutputStream[] toPlayer;

    private int[] cardValues;
    private int[] scores;
    private int matches;


    /**
     * Constructor for MemoryGameService
     * @param p the player
     */
    public MemoryGameService(Socket[] p) {
        players = new Socket[p.length];
        fromPlayer = new DataInputStream[p.length];
        toPlayer = new DataOutputStream[p.length];
        scores = new int[p.length];

        //initialize the player
        for(int i = 0; i < players.length; i++)
            players[i] = p[i];

        cardValues = new int[PLAYING_CARDS];

        //initialize the the card values
        for(int i = 0, j = 0; i < cardValues.length; i++) {
            if(i != 0 && i % 2 == 0)
                j++;

            cardValues[i] = j;
        }
        //shuffle the cards
        shuffleCards();
    }

    /**
     * Determines the flow of the program. The server responds and sends instructions to the client
     * based on the button the client pressed.
     */
    public void run() {
        try {

            //initialize the input and output
            for(int i = 0; i < players.length; i++) {
                fromPlayer[i] = new DataInputStream(players[i].getInputStream());
                toPlayer[i] = new DataOutputStream(players[i].getOutputStream());
            }

            int index = 0;

            while(true) {

                //determine if the match is over
                if(matches == MAX_MATCHES) {
                    finishGame();
                }

                //tell player to start
                toPlayer[index].writeInt(PLAY);

                //other players must wait until their turn
                for(int i = 0; i < toPlayer.length; i++) {
                    if(!toPlayer[index].equals(toPlayer[i]))
                        toPlayer[i].writeInt(WAIT);
                }


                //check user selection
                int card1 = fromPlayer[index].readInt();
                if(card1 == QUIT) { //check if user pressed quit or not
                    scores[index] = -1;
                    finishGame();
                }
                display(card1);

                //check user selection
                toPlayer[index].writeInt(PLAY);
                int card2 = fromPlayer[index].readInt();
                if(card2 == QUIT) { //check if user pressed quit or not
                    scores[index] = -1;
                    finishGame();
                }
                display(card2);

                //if the two cards match, give the player a point
                if(isMatch(card1, card2)) {
                    toPlayer[index].writeInt(SCORE);
                    matches++;
                    scores[index]++;
                    toPlayer[index].writeInt(scores[index]);
                }

                //if there was no match hide the cards
                else {
                    hide();
                }

                //move on to next player
                index++;

                //go back to the first players turn when at the end
                if(index == players.length)
                    index = 0;

            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Shuffles the cards in a random order
     */
    private void shuffleCards() {
        int j;
        int tmp;
        Random rn = new Random();
        for(int i = 0; i < cardValues.length; i++) {
            j = rn.nextInt(i + 1);
            tmp = cardValues[j];
            cardValues[j] = cardValues[i];
            cardValues[i] = tmp;
        }
    }

    /**
     * Determines whether the two cards selected by the user are a match, and sends back to the
     * client if the match existed or not
     * @param card1 the first card
     * @param card2 the second card
     * @return if a match has been made or not
     * @throws IOException throws an exception
     */
    private boolean isMatch(int card1, int card2) throws IOException {
        if(cardValues[card1] != cardValues[card2])
            return false;

        for(int i = 0; i < players.length; i++) {
            toPlayer[i].writeInt(MATCH);
            toPlayer[i].writeInt(card1);
            toPlayer[i].writeInt(card2);
            toPlayer[i].flush();
        }


        return true;
    }

    /**
     * Tells the users the conditions to the game has been met and the game
     * is over. Determines who wins the game based on the final score.
     */
    private void finishGame() {
        try {
            int max = scores[0];

            for(int i = 0; i < players.length; i++)
                toPlayer[i].writeInt(FINISH);

            //find the highest score
            for(int i = 0; i < scores.length; i++) {
                if(max < scores[i])
                    max = scores[i];
            }

            for(int i = 0; i < scores.length; i++) {
                if(max == scores[i])
                    toPlayer[i].writeInt(WINNER);
                if(max > scores[i])
                    toPlayer[i].writeInt(LOSER);
            }
//
        } catch(IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Displays the card selected to all the players of the game
     * @param card the card selected by the player
     * @throws IOException throws an exception
     */
    private void display(int card) throws IOException {
        for(int i = 0; i < players.length; i++) {
            toPlayer[i].writeInt(DISPLAY);
            toPlayer[i].writeInt(card);
            toPlayer[i].writeInt(cardValues[card]);
            toPlayer[i].flush();
        }



    }

    /**
     * Tells the clients that the cards are going to be hidden since no
     * match was made
     * @throws IOException throws an exception
     */
    private void hide() throws IOException {
        for(int i = 0; i < players.length; i++) {
            toPlayer[i].writeInt(HIDE);
        }
    }


}

/**
 * Created by alextestani on 2017-04-02.
 */
public interface MemoryGameConstants {

    int ROWS = 3; //rows of cards
    int COLUMNS = 3; //columns of cards
    int PLAYING_CARDS = ROWS * COLUMNS;
    int PORT = 1181;
    int MAX_MATCHES = PLAYING_CARDS / 2;

    /**
     * message sent by server to the client
     * PLAY takes no arguments
     * PLAY tells the client that it is his/her turn
     */
    int PLAY = 1000;

    /**
     * response sent by server to the client
     * SCORE takes an integer n with it
     * n is the current number of the clients successful matches
     * SCORE is sent when the client successfully matches two cards together
     */
    int SCORE = 1001;
    /**
     * message sent by server to the client
     * WAIT takes no arguements
     * WAIT tells the client to wait for his/her turn
     */
    int WAIT = 1002;

    /**
     * message sent by server to the client
     * WINNER takes no arguments
     * WINNER tells the client that he/she won the game
     */
    int WINNER = 1003;

    /**
     * message sent by server to the client
     * LOSER takes no arguments
     * LOSER tells the client that he/she lost
     */
    int LOSER = 1004;

    /**
     * response sent by server to the client
     * DISPLAY takes two arguments i, v
     * where i is the index of the card and v is the value of the card
     * DISPLAY displays the value of the selected card from the client
     */
    int DISPLAY = 1005;

    /**
     * response sent by server to the client
     * HIDE takes no arguments
     * HIDE puts the cards back to their "face down" state and sends this information
     * to the client
     */
    int HIDE = 1006;

    /**
     * response sent by server to the client
     * MATCH takes no arguments
     * MATCH determines if the two cards the player picked matched, if the cards
     * do match the player is sent SCORE which adds a point to the players score
     */
    int MATCH = 1008;

    /**
     * message sent by server to the client
     * FINISH takes no arguments
     * FINISH alerts the clients that the game is over and then determines the results
     * by sending WINNER or LOSER based on the clients number of matches
     */
    int FINISH = 1009;

    /**
     * message sent by server to the client
     * QUIT takes no arguments
     * QUIT alerts the server that the client is no longer playing
     * the server responds by setting the other player to the winner
     */
    int QUIT = 9000;


}

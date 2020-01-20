import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.*;
import java.io.*;
import java.net.InetAddress;

/**
 * A frame used to play the Memory Game
 */
public class MemoryGameClient extends JFrame implements MemoryGameConstants, Runnable{

    private static final int FRAME_WIDTH = 450;
    private static final int FRAME_HEIGHT = 500;

    private Card[] playingCards;

    private boolean continuePlaying;

    private JButton quitButton;
    private JLabel statusLabel;
    private JLabel playerLabel;
    private JLabel scoreLabel;
    private JPanel layout;
    private int cardSelected;

    private Socket socket;
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    private boolean myTurn;
    private boolean waiting = true;

    /**
     * The main method used to run the MemoryGameClient
     * @param args used to display information
     * @throws UnknownHostException throws an exception
     */
    public static void main(String[] args) throws UnknownHostException {
        InetAddress ip;
        ip = InetAddress.getLocalHost();
        String hostAddr = ip.getHostAddress();

        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-server"))
                System.out.println(hostAddr);
            if(args[i].equals("-help"))
                printHelp();
            if(args[i].equals("-img"))
                System.out.println("MemoryGameV2/images");

        }

        MemoryGameClient client = new MemoryGameClient(ip);
    }

    /**
     * Creates a frame for the MemoryGame client
     */
    public MemoryGameClient(InetAddress ip) {
        playingCards = new Card[PLAYING_CARDS];
        createCards();
        createButton();
        createLabel();
        createPanel();

        setTitle("Memory Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setVisible(true);

        connect(ip);
        playGame();
    }

    /**
     * Prints instructions on how to play the Memory Game
     */
    private static void printHelp() {
        System.out.println("***************");
        System.out.println("Welcome to my memory game! When it's your turn you may click two cards," + '\n' +
                "if the two cards match you get a point and then it's the other players turn!" + '\n' +
                "If the two cards you pick don't match, you get three seconds to memorize them" + '\n' +
                "until they are hidden. Then it is the others player turn to guess! " + '\n' +
                "If you don't feel like playing anymore, just click the quit button. " + '\n' +
                "Have fun and thank you for playing");
        System.out.println("***************");

    }

    /**
     * Connects the client to the server
     * @param ip the host ip address
     */
    public void connect(InetAddress ip) {
        try {
            socket = new Socket(ip, PORT);

            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch(IOException e) {

        }

    }

    /**
     * Initializes the playing cards
     */
    private void createCards() {

        for(int i = 0; i < playingCards.length; i++) {
            playingCards[i] = new Card();
            playingCards[i].addActionListener(new ButtonListener());
        }
    }

    /**
     * Creates a quit button
     */
    private void createButton() {
        quitButton = new JButton("quit");
        quitButton.addActionListener(e -> playerQuit());
    }

    //prompts the server user has quit
    private void playerQuit() {
        try {
            if (myTurn) {
                toServer.writeInt(QUIT);
                myTurn = false;
                waiting = false;
            }
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Creates labels to display the players status, score, and player number
     */
    private void createLabel() {
        statusLabel = new JLabel("Please wait");
        scoreLabel = new JLabel("SCORE: " + 0);
        playerLabel = new JLabel("PLAYER");
    }


    /**
     * Halts the client until a player has made a move
     * @throws InterruptedException throws an exception
     */
    public void waitingForMove() throws InterruptedException {
        while(waiting)
            Thread.sleep(300);

        waiting = true;
    }

    /**
     * Reads in the index and value of the selected card from the server
     * and displays it for the client to view
     */
    public void displayCard() throws IOException {
        int index = fromServer.readInt();
        int value = fromServer.readInt();

        playingCards[index].setIcon(new ImageIcon("images/" + value + ".jpg"));
//        playingCards[index].setText("" + value);
    }

    /**
     * Changes the buttons back to their initial state. The players are given
     * three seconds to remember the cards.
     */
    public void hideCards() throws IOException, InterruptedException {
        for(int i = 3; i > 0; i--) {
            statusLabel.setText(i + "...");
            Thread.sleep(1000);
        }

        for(int i = 0; i < playingCards.length; i++) {
            playingCards[i].select(false);
            if (playingCards[i].isMatch() == false)
                playingCards[i].setIcon(new ImageIcon(Card.DEFAULT_CARD));
        }

    }


    /**
     * Sends the clients move to the server, and sets the cards state to a selected
     * state meaning it can no longer be chosen until it is unselected.
     * @throws IOException throws an exception
     */
    public void sendMove() throws IOException {
        playingCards[cardSelected].select(true);
        toServer.writeInt(cardSelected);
    }

    /**
     * Creates the panel to display the Clients information
     */
    public void createPanel() {
        JPanel statusLayout = new JPanel(new BorderLayout());
        statusLayout.setBorder(new EmptyBorder(10, 10, 10, 10));
        statusLayout.add(quitButton, BorderLayout.WEST );
        statusLayout.add(statusLabel, BorderLayout.CENTER);
        statusLayout.add(scoreLabel, BorderLayout.EAST);

        JPanel card = new JPanel(new GridLayout(ROWS, COLUMNS));
        for(int i = 0; i < playingCards.length; i++) {
            playingCards[i].setBorder(new LineBorder(Color.BLACK));
            card.add(playingCards[i]);
        }

        layout = new JPanel(new BorderLayout());
        layout.add(playerLabel, BorderLayout.NORTH);
        layout.add(card, BorderLayout.CENTER);
        layout.add(statusLayout, BorderLayout.SOUTH);

        add(layout);
    }


    /**
     * Disables the cards, rendering them un-clickable
     */
    public void disableButtons() {
        for(int i = 0; i < playingCards.length; i++) {
            playingCards[i].setEnabled(false);
        }
    }

    /**
     * Adds a point to the players score
     */
    public void addPoint() throws IOException {
        scoreLabel.setText("SCORE: " + fromServer.readInt());
    }

    /**
     * Reads in information from the server and sets the cards to a revealed state
     * meaning they can no longer be selected
     * @throws IOException throws an exception
     */
    private void revealCards() throws IOException {
        int i = fromServer.readInt();
        int j = fromServer.readInt();
        playingCards[i].setMatch();
        playingCards[j].setMatch();

    }

    /**
     * Reads in information from the server and determines who wins and who loses
     * based on the players score. The game then ends.
     * @throws IOException throws an exception
     */
    private void displayResults() throws IOException {
        int cmd = fromServer.readInt();

        switch(cmd) {
            case WINNER:
                statusLabel.setText("YOU WIN!");
                break;
            case LOSER:
                statusLabel.setText("YOU LOSE...");
                break;
        }

        continuePlaying = false;
    }

    private void playGame() {
        continuePlaying = true;
        for(int i = 0; i < playingCards.length; i++)
            playingCards[i].setEnabled(true);
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * This method is used to to determine the flow of the game, the client sends the server
     * the index of the card selected and the server responds with information to determine the
     * current state of the game.
     */
    public void run() {
        try {

            int player = fromServer.readInt();

            playerLabel.setText("PLAYER " + player);
            statusLabel.setText("Waiting for other players to join...");

            int cmd = fromServer.readInt();

            while(continuePlaying) {
                switch(cmd) {
                    case PLAY:
                        statusLabel.setText("Your turn!");
                        myTurn = true;
                        waitingForMove();
                        sendMove();
                        break;
                    case WAIT:
                        statusLabel.setText("Wait for your turn.");
                        break;
                    case DISPLAY:
                        displayCard();
                        break;
                    case SCORE:
                        addPoint();
                        break;
                    case HIDE:
                        hideCards();
                        break;
                    case MATCH:
                        revealCards();
                        break;
                    case FINISH:
                        displayResults();
                        break;

                }

                cmd = fromServer.readInt();
            }

        }
        catch (IOException e) {
        }
        catch (InterruptedException e) {
        }

        //end game
        disableButtons();
    }


    /**
     * Stores the index of the card selected into a variable. Also determines whether
     * a card has already been selected or not
     */
    public class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if(myTurn) {
                JButton sourceButton = (JButton) e.getSource();
                for (int i = 0; i < playingCards.length; i++)
                    if (playingCards[i].equals(sourceButton)) {
                        if(playingCards[i].isMatch() || playingCards[i].isSelected()) {
                            statusLabel.setText("That card has already been picked.");
                        }
                        else {
                            cardSelected = i;
                            myTurn = false;
                            waiting = false;
                        }
                    }
            }
            else
                statusLabel.setText("You can't choose a card right now!");
        }

    }


}





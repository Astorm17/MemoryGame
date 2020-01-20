import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.Date;

public class MemoryGameServer extends JFrame implements MemoryGameConstants {

    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 300;
    private JTextArea textLog;
    private JScrollPane scrollPane;
    private ServerSocket serverSocket;
    private Socket[] players;
    private static int numOfPlayers;

    /**
     * Constructor for MemoryGameServer, creates a frame and sets up
     * a server for the clients to connect to
     */
    public MemoryGameServer() {

        players = new Socket[numOfPlayers];
        textLog = new JTextArea();
        scrollPane = new JScrollPane(textLog);

        add(scrollPane);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setTitle("Memory Game Server");
        setVisible(true);

        try {
            serverSocket = new ServerSocket(PORT);
            textLog.append(new Date() + ": Start server at socket " + PORT + '\n'");

            int sessionNum = 1;

            while(true) {
                for (int i = 0; i < players.length; i++) {
                    players[i] = serverSocket.accept();
                    textLog.append(new Date() + " PLAYER " + (i + 1) + " has joined session " + sessionNum + '\n');
                    new DataOutputStream(players[i].getOutputStream()).writeInt(i + 1);
                }

                textLog.append(new Date() + ": Start a thread for session " + sessionNum++ + '\n'");

                MemoryGameService service = new MemoryGameService(players);

                new Thread(service).start();
            }

        } catch(IOException e) {
            System.out.println(e);
        }

    }

    /**
     * main method for the server
     * -num numOfPlayers determines the players that will be able to connect to the server per session
     * @param args numOfPlayers
     */
    public static void main(String[] args) {
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-num"))
                numOfPlayers = Integer.parseInt(args[i+1]);
        }

        MemoryGameServer server = new MemoryGameServer();
    }
}

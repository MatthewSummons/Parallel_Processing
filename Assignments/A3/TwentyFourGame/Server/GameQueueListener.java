package TwentyFourGame.Server;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import TwentyFourGame.Common.GameStartMessage;
import TwentyFourGame.Common.UserData;
import TwentyFourGame.Server.GamePublisher;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class GameQueueListener implements MessageListener {

    private Connection connection;
    private Session session;

    // Game join logic state
    private final ArrayList<UserData> waitingPlayers = new ArrayList<>();
    private long firstJoinTime = 0;
    private Timer joinTimer = null;
    private boolean timerFired = false;
    private final Object lock = new Object();

    private GamePublisher gamePublisher;

    public void startListening() throws Exception {
        System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

        Context jndiContext = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                .lookup("jms/JPoker24GameConnectionFactory");
        Queue queue = (Queue) jndiContext.lookup("jms/JPoker24GameQueue");

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        gamePublisher = new GamePublisher(session);
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(this);
        connection.start();

        System.out.println("Game Queue & Game Publisher is live ...");
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                Object obj = ((ObjectMessage) message).getObject();
                if (obj instanceof UserData) {
                    UserData userData = (UserData) obj;
                    System.out.println("Received UserData object: " + userData.username);

                    synchronized (lock) {
                        waitingPlayers.add(userData);

                        if (waitingPlayers.size() == 1 && joinTimer == null) {
                            // First player joined, start timer
                            firstJoinTime = System.currentTimeMillis();
                            timerFired = false;
                            joinTimer = new Timer();
                            joinTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    System.out.println("Timer fired after 10 seconds.");
                                    synchronized (lock) {
                                        timerFired = true;
                                        // Only start game if at least 2 players are present
                                        if (waitingPlayers.size() >= 2) {
                                            startGame();
                                        }
                                        // If only 1 player, do nothing: keep them in the queue
                                    }
                                }
                            }, 10000); // 10 seconds
                        }

                        // Start game immediately if 4 players
                        if (waitingPlayers.size() == 4) {
                            startGame();
                        }

                        // If timer already fired and now we have 2+ players, start game immediately
                        if (timerFired && waitingPlayers.size() >= 2) {
                            startGame();
                        }
                    }
                }
            } else {
                System.out.println("Received non-object message: " + message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e);
        }
    }

    // Helper to start the game and reset state
    private void startGame() {
        System.out.println("Starting game with players:");
        for (UserData user : waitingPlayers) {
            System.out.println("  - " + user.username);
        }
        GameStartMessage startMsg = new GameStartMessage();
        startMsg.cards = generateRandomCards(); // Implement this utility
        startMsg.players = new ArrayList<>(waitingPlayers);
        startMsg.gameId = java.util.UUID.randomUUID().toString();
        startMsg.startTime = System.currentTimeMillis();

        try {
            gamePublisher.publishGameStart(startMsg);
            System.out.println("GameStartMessage published to topic.");
        } catch (JMSException e) {
            System.err.println("Failed to publish GameStartMessage: " + e);
        }

        // Reset state
        waitingPlayers.clear();
        firstJoinTime = 0;
        timerFired = false;
        if (joinTimer != null) {
            joinTimer.cancel();
            joinTimer = null;
        }
    }
    
    // Optional: call this to clean up resources when shutting down
    public void stopListening() {
        try {
            if (session != null)    session.close();
            if (connection != null) connection.close();
        } catch (Exception e) {
            System.err.println("Error closing JMS resources: " + e);
        }
    }

    private ArrayList<String> generateRandomCards() {
        // Returns a list of strings representing random cards where the suits are utf emojis
        ArrayList<String> cards = new ArrayList<>();
        String[] suits = {"♠", "♥", "♦", "♣"};
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        
        for (int i = 0; i < 4; i++) {
            String suit = suits[(int) (Math.random() * suits.length)];
            String rank = ranks[(int) (Math.random() * ranks.length)];
            cards.add(rank + suit);
        }
        
        return cards;
    }
}
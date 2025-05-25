package TwentyFourGame.Server;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import TwentyFourGame.Common.GameOverMessage;
import TwentyFourGame.Common.GameStartMessage;
import TwentyFourGame.Common.UserData;
import TwentyFourGame.Server.GamePublisher;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class GameQueueListener implements MessageListener {

    private Connection connection;
    public Session session;
    private Queue queue;

    // Game join logic state
    private final ArrayList<UserData> waitingPlayers = new ArrayList<>();
    private long firstJoinTime = 0;
    private boolean inGame = false;
    private Timer joinTimer = null;
    private boolean timerFired = false;
    private long gameStartTime = 0;
    private final Object lock = new Object();

    private GamePublisher gamePublisher;

    public GameQueueListener() throws Exception{
        System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

        Context jndiContext = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                .lookup("jms/JPoker24GameConnectionFactory");
        queue = (Queue) jndiContext.lookup("jms/JPoker24GameQueue");
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    }

    public void setGamePublisher(GamePublisher publisher) {
        this.gamePublisher = publisher;
        System.out.println("GamePublisher bound to GameQueueListener.");
    }

    public void startListening() throws Exception {
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(this);
        connection.start();
        System.out.println("Game Queue is listening ...");
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                Object obj = ((ObjectMessage) message).getObject();
                if (obj instanceof UserData) {
                    UserData userData = (UserData) obj;
                    System.out.println("Received UserData object: " + userData.username);
                    handleUserJoin(userData);
                } else if (obj instanceof GameOverMessage) {
                    GameOverMessage gameOverMessage = (GameOverMessage) obj;
                    System.out.println("Received GameOverMessage: " + gameOverMessage.winnerUsername);
                    handleGameOver(gameOverMessage);
                } else {
                    System.out.println("Received unknown object type: " + obj.getClass().getSimpleName());
                }
            } else {
                System.out.println("Received non-object message: " + message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e);
        }
    }
    
    private void handleUserJoin(UserData userData) {
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
    
    // Helper to start the game and reset state
    private void startGame() {
        inGame = true;
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
            gameStartTime = System.currentTimeMillis();
            System.out.println("GameStartMessage published to topic.");
        } catch (JMSException e) {
            System.err.println("Failed to publish GameStartMessage: " + e);
        }

        // Reset state (Still in game until game over)
        waitingPlayers.clear();
        firstJoinTime = 0;
        timerFired = false;
        if (joinTimer != null) {
            joinTimer.cancel();
            joinTimer = null;
        }
    }
    
    private void handleGameOver(GameOverMessage msg) {
        if (!inGame) {
            System.out.println("Received GameOverMessage but no game is currently active.");
            return;
        } inGame = false; // Reset game state

        try {
            long duration = System.currentTimeMillis() - gameStartTime;
            System.out.println("Game over! Winner: " + msg.winnerUsername + ", Duration: " + duration + "ms");
            gamePublisher.publishGameOver(msg);
            System.out.println("GameOverMessage published to topic.");
        } catch (JMSException e) {
            System.err.println("Failed to publish GameOverMessage: " + e);
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
        
        java.util.Set<String> uniqueCards = new java.util.HashSet<>();
        while (uniqueCards.size() < 4) {
            String suit = suits[(int) (Math.random() * suits.length)];
            String rank = ranks[(int) (Math.random() * ranks.length)];
            uniqueCards.add(rank + suit);
        } cards.addAll(uniqueCards);
        
        return cards;
    }
}
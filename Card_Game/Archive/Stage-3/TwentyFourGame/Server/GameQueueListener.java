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

    private final Connection connection;
    public final Session session;
    private final Queue queue;

    // Game join logic state
    private final ArrayList<UserData> waitingPlayers = new ArrayList<>();
    private ArrayList<UserData> inGamePlayers = new ArrayList<>();
    private long firstJoinTime = 0;
    private boolean inGame = false;
    private Timer joinTimer = null;
    private boolean timerFired = false;
    private long gameStartTime = 0;
    private final Object lock = new Object();

    private GamePublisher gamePublisher;
    private final DatabaseManager DB;

    public GameQueueListener(DatabaseManager db) throws Exception{
        DB = db;
        
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
        if (inGame) {
            System.out.println("Received UserData but already in game, ignoring: " + userData.username);
            return; // Ignore if already in game
        }
        
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
        startMsg.cards = generateRandomCards();
        startMsg.players = new ArrayList<>(waitingPlayers);
        startMsg.startTime = System.currentTimeMillis();

        inGamePlayers = new ArrayList<>(waitingPlayers);

        try {
            gamePublisher.publishGameStart(startMsg);
            gameStartTime = System.currentTimeMillis();
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
    
    private void handleGameOver(GameOverMessage msg) {
        if (!inGame) {
            System.out.println("Received GameOverMessage but no game is currently active.");
            return;
        }

        try {
            long duration = System.currentTimeMillis() - gameStartTime;
            System.out.println("Game over! Winner: " + msg.winnerUsername + ", Duration: " + duration + "ms");
            for (UserData player : inGamePlayers) {
                UserData dbUser = DB.readUserInfo(player.username);
                int newGames = dbUser.games + 1;
                int newWins = dbUser.wins;
                double newAvgWinTime = dbUser.avgWinTime;

                if (player.username.equals(msg.winnerUsername)) {
                    newWins += 1;
                    // Recalculate average win time (~2dp)
                    newAvgWinTime = Math.round(((dbUser.avgWinTime * dbUser.wins) + (duration / 1000.0)) / newWins * 100.0) / 100.0;
                }

                DB.updateUserTable(player.username, newWins, newGames, newAvgWinTime);
            }
            gamePublisher.publishGameOver(msg);
            System.out.println("GameOverMessage published to topic.");
        } catch (JMSException e) {
            System.err.println("Failed to publish GameOverMessage: " + e);
        } finally {
            inGame = false;
            inGamePlayers.clear();
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
        
        java.util.Set<String> usedRanks = new java.util.HashSet<>();
        while (cards.size() < 4) {
            String rank = ranks[(int) (Math.random() * ranks.length)];
            if (!usedRanks.contains(rank)) {
                String suit = suits[(int) (Math.random() * suits.length)];
                cards.add(rank + suit);
                usedRanks.add(rank);
            }
        }
        
        return cards;
    }
}
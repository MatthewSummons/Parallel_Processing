package TwentyFourGame.Server;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import TwentyFourGame.Common.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GameQueueListener implements MessageListener {

    private Connection connection;
    private Session session;

    // Game join logic state
    private final List<UserData> waitingPlayers = new ArrayList<>();
    private long firstJoinTime = 0;
    private Timer joinTimer = null;
    private boolean timerFired = false;
    private final Object lock = new Object();

    public void startListening() throws Exception {
        System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

        Context jndiContext = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                .lookup("jms/JPoker24GameConnectionFactory");
        Queue queue = (Queue) jndiContext.lookup("jms/JPoker24GameQueue");

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(this);
        connection.start();

        System.out.println("Game Queue Listener is now listening for messages...");
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
        // TODO: Inform clients via topic, start game logic, etc.

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
}
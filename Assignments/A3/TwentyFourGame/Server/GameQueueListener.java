package TwentyFourGame.Server;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import TwentyFourGame.Common.UserData;

public class GameQueueListener implements MessageListener {

    private Connection connection;
    private Session session;

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
                ObjectMessage objMessage = (ObjectMessage) message;
                Object obj = objMessage.getObject();
                if (obj instanceof UserData) {
                    UserData userData = (UserData) obj;
                    System.out.println("Received UserData object:");
                    System.out.println("  Username: " + userData.username);
                } else {
                    System.out.println("Received ObjectMessage, but not UserData: " + obj.getClass().getName());
                }
            } else {
                System.out.println("Received non-text, non-object message: " + message.getClass().getSimpleName());
            }
        } catch (JMSException e) {
            System.err.println("Error processing message: " + e);
        }
    }

    // Optional: call this to clean up resources when shutting down
    public void stopListening() {
        try {
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
        } catch (Exception e) {
            System.err.println("Error closing JMS resources: " + e);
        }
    }
}
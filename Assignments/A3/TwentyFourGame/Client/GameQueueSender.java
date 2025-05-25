package TwentyFourGame.Client;

import java.util.Properties;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import TwentyFourGame.Common.GameOverMessage;
import TwentyFourGame.Common.UserData;

public class GameQueueSender {
    private Context ctx;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    
    public GameQueueSender()  throws Exception {
        // Set ORB properties for GlassFish remote JNDI
        System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");

        this.ctx = new InitialContext(props);

        ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("jms/JPoker24GameConnectionFactory");
        Queue queue = (Queue) ctx.lookup("jms/JPoker24GameQueue");

        this.connection = connectionFactory.createConnection();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.producer = session.createProducer(queue);
    }


    public void sendUserData(UserData userData) throws Exception {
        ObjectMessage objMessage = session.createObjectMessage(userData);
        this.producer.send(objMessage);
    }

    public void sendGameOverMessage(GameOverMessage msg) throws Exception {
        ObjectMessage objMessage = session.createObjectMessage(msg);
        this.producer.send(objMessage);
    }

    public void close() {
        try {
            if (producer != null)   producer.close();
            if (session != null)    session.close();
            if (connection != null) connection.close();
            if (ctx != null) ctx.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
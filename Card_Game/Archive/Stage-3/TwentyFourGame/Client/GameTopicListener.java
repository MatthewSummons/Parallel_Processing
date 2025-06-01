package TwentyFourGame.Client;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import TwentyFourGame.Common.GameOverMessage;
import TwentyFourGame.Common.GameStartMessage;

public class GameTopicListener implements Runnable {
    private final AppPanel appPanel;

    public GameTopicListener(AppPanel appPanel) {
        this.appPanel = appPanel;
    }

    @Override
    public void run() {
        try {
            System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
            System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
            
            Context ctx = new InitialContext();
            
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup("jms/JPoker24GameConnectionFactory");
            Topic topic = (Topic) ctx.lookup("jms/JPoker24GameTopic");
            Connection connection = factory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer subscriber = session.createConsumer(topic);
            subscriber.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        if (message instanceof ObjectMessage) {
                            Object obj = ((ObjectMessage) message).getObject();
                            if (obj instanceof GameStartMessage) {        // Game Start
                                GameStartMessage startMsg = (GameStartMessage) obj;
                                javax.swing.SwingUtilities.invokeLater(() -> appPanel.showGamePanel(startMsg));
                            } else if (obj instanceof GameOverMessage) {  // Game End
                                GameOverMessage overMsg = (GameOverMessage) obj;
                                javax.swing.SwingUtilities.invokeLater(() -> appPanel.showGameOverPanel(overMsg));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            connection.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
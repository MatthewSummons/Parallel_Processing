package TwentyFourGame.Server;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import TwentyFourGame.Common.GameStartMessage;
import TwentyFourGame.Common.GameOverMessage;

public class GamePublisher {
    private final Session session;
    private final MessageProducer producer;

    public GamePublisher(Session session) throws Exception {
        Context ctx = new InitialContext();
        Topic topic = (Topic) ctx.lookup("jms/JPoker24GameTopic");
        this.session = session;
        this.producer = session.createProducer(topic);
    }

    public void publishGameStart(GameStartMessage msg) throws JMSException {
        ObjectMessage objMsg = session.createObjectMessage(msg);
        producer.send(objMsg);
    }

    public void publishGameOver(GameOverMessage msg) throws JMSException {
        ObjectMessage objMsg = session.createObjectMessage(msg);
        producer.send(objMsg);
    }
}
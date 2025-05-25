package TwentyFourGame.Common;

import java.io.Serializable;

public final class GameOverMessage implements Serializable {
    public final String winnerUsername;
    public final String winningExpression;

    public GameOverMessage(String winnerUsername, String winningExpression) {
        this.winnerUsername = winnerUsername;
        this.winningExpression = winningExpression;
    }
}
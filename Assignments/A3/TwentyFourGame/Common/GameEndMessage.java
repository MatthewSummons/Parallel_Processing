package TwentyFourGame.Common;

import java.io.Serializable;

public class GameEndMessage implements Serializable {
    public String gameId;
    public String winnerUsername;
    public long winningTime;
}
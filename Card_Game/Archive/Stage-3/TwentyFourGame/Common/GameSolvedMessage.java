package TwentyFourGame.Common;

import java.io.Serializable;

public class GameSolvedMessage implements Serializable {
    public String gameId;
    public String username;
    public long solveTime;
}
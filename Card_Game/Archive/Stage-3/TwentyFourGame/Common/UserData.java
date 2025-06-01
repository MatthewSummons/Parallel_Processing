package TwentyFourGame.Common;

import java.io.Serializable;

public final class UserData implements Serializable {
    public static final int MAX_USRERNAME_LENGTH = 40;
    
    public final String username;
    public final int wins;
    public final int games;
    public final double avgWinTime;
    public final int rank;

    public UserData(
        String username, int wins, int games, double avgWinTime, int rank
    ) {
        this.username = username;
        this.wins = wins;
        this.games = games;
        this.avgWinTime = avgWinTime;
        this.rank = rank;
    }

    @Override
    public String toString() {
        return wins + ", " + games + ", " + avgWinTime + ", " + rank;
    }
}

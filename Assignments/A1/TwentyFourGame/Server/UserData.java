package TwentyFourGame.Server;

import java.io.Serializable;

public final class UserData  implements Serializable {
    private static final long serialVersionUID = 1L;

    public String username;
    public int wins;
    public int games;
    public double avgWinTime;
    public int rank;

    public UserData(String username, int wins, int games, double avgWinTime, int rank) {
        this.username = username;
        this.wins = wins;
        this.games = games;
        this.avgWinTime = avgWinTime;
        this.rank = rank;
    }

    public String toString() {
        return wins + ", " + games + ", " + avgWinTime + ", " + rank;
    }
}

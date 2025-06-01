package TwentyFourGame.Common;

import java.io.Serializable;
import java.util.List;

public class GameStartMessage implements Serializable {
    public List<String> cards;
    public List<UserData> players;
    public String gameId;
    public long startTime;

    // For each user return an array of their username, wins and avgWinTime
    public String[][] getPlayerData() {    
        String[][] playerData = new String[players.size()][3];
        for (int i = 0; i < players.size(); i++) {
            UserData user = players.get(i);
            playerData[i][0] = user.username;
            playerData[i][1] = "Wins: " + user.wins + " / " + user.games;
            playerData[i][2] = "AvgWinTime: " + user.avgWinTime + "s";
        }

        return playerData;
    }
}
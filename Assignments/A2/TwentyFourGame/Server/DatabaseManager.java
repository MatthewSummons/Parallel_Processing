package TwentyFourGame.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseManager {

    // TODO: Get from .env files later
    private static final String DB_HOST = "localhost";
    private static final String DB_USER = "u3035946760";
    private static final String DB_PASS = "C3358PASS";
    private static final String DB_NAME = "TwentyFour";
    
    private static final String USR_TBL = "UserInfo";
    private static final String ONL_TBL = "OnlineUser";

    private ReentrantReadWriteLock UserInfoLock   = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock OnlineUserLock = new ReentrantReadWriteLock();

    private Connection conn;   
    public DatabaseManager() {
        
        LoadDriver(DB_HOST, DB_USER, DB_PASS, DB_NAME);
        CheckNeededTables(DB_NAME, USR_TBL, ONL_TBL);
        // Clear Login Sessions Table;
        ClearTable(ONL_TBL);

        System.out.println("Database connection successful");
    }
    
    private void LoadDriver(
        String DB_HOST, String DB_USER, String DB_PASS, String DB_NAME
    ) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            System.err.println("Error loading MySQL driver: " + e); System.exit(1);
        } try {
            conn = DriverManager.getConnection("jdbc:mysql://" +
                DB_HOST + "/" + DB_NAME + "?user=" + DB_USER + "&password=" + DB_PASS
            );
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e); System.exit(1);
        }

    }

    private void CheckNeededTables(
        String DB_NAME, String UserTable, String OnlineTable
    ) {
        try{
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables " + 
                "WHERE table_schema = ? AND table_name IN (?, ?)"
            );    
            
            stmt.setString(1, DB_NAME);
            stmt.setString(2, USR_TBL);
            stmt.setString(3, ONL_TBL);
            
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.err.println("Error: Unable to check if tables exist in the database");
                System.exit(1);
            } else if (rs.getInt(1) != 2) {
                System.err.println("Error: Required tables do not exist in the database");
                System.exit(1);
            }
        } catch (SQLException e) {
            System.err.println("Error in loading tables: " + e); 
            System.exit(1);
        }

    }

    // WARNING: DELETES ALL ROWS FROM A TABLE! PROCEED WITH CAUTION!
    private void ClearTable(String tableName) {
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + tableName);
            stmt.execute();
        } catch (SQLException e) { System.err.println("Error clearing table: " + e); }
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Database Methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    
    public RegisterStatus register(String username, String passHash) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO " + USR_TBL + " (username, passHash, wins, games, avgWinTime)" +
                " VALUES (?, ?, 0, 0, 0)");
            stmt.setString(1, username);
            stmt.setString(2, passHash);
            
            UserInfoLock.writeLock().lock();
            stmt.executeUpdate();
            return RegisterStatus.SUCCESS;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                return RegisterStatus.USERNAME_TAKEN;
            } System.err.println("Error registering user: " + e);
            return RegisterStatus.SERVER_ERROR;
        } finally { UserInfoLock.writeLock().unlock(); }
    }
    
    public UserData readUserInfo(String username) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM (" +
                "  SELECT username, wins, games, avgWinTime, " + 
                "  dense_rank() OVER (ORDER BY wins desc) AS player_rank " +  
                "  FROM " + USR_TBL +
                ") ranked_users " +
                "WHERE username = ?"
            ); stmt.setString(1, username);
            
            UserInfoLock.readLock().lock();
            ResultSet rs = stmt.executeQuery();   
            if (rs.next()) {
                return new UserData(
                    rs.getString("username"),
                    rs.getInt("wins"),
                    rs.getInt("games"),
                    rs.getDouble("avgWinTime"),
                    rs.getInt("player_rank")
                );
            } else { return null; }
        } catch (SQLException e) {
            System.err.println("Error reading user info: " + e);
            return null;
        } finally { UserInfoLock.readLock().unlock(); }
    }
    
    /*
     * Checks if user is registered, and provided password is correct,
     * then checks if user is already logged in. If not, adds them to 
     * the online user table.  
    */
    public LoginStatus login(String username, String passHash) {
        UserInfoLock.readLock().lock();
        OnlineUserLock.writeLock().lock();
        try {
            conn.setAutoCommit(false);  // Start transaction
            
            PreparedStatement login_stmt = conn.prepareStatement(
                "SELECT username, passHash FROM " + USR_TBL + " WHERE username = ?"
            ); login_stmt.setString(1, username);

            ResultSet rs = login_stmt.executeQuery();
            if (rs.next() && rs.getString("passHash").equals(passHash)) {
                PreparedStatement online_stmt = conn.prepareStatement(
                    "INSERT INTO " + ONL_TBL + " (username) VALUES (?)"
                ); online_stmt.setString(1, username);
                
                int rowsAffected = online_stmt.executeUpdate();
                if (rowsAffected == 1) {
                    conn.commit();  // Commit transaction
                    return LoginStatus.SUCCESS;
                } else {
                    conn.rollback();  // Rollback if insert fails
                    return LoginStatus.SERVER_ERROR;
                }
            } else {
                conn.rollback();  // Rollback if credentials invalid
                return LoginStatus.INVALID_CREDENTIALS;
            }
        } catch (SQLException e) {
            try {
                conn.rollback();  // Rollback on exception
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx);
            } if (e.getErrorCode() == 1062) { // Duplicate entry
                return LoginStatus.ALREADY_LOGGED_IN;
            }  System.err.println("Error logging in user: " + e);
            return LoginStatus.SERVER_ERROR;
        } finally { 
            
            try {
                conn.setAutoCommit(true);  // Reset auto-commit
            } catch (SQLException resetEx) {
                System.err.println("Error resetting auto-commit: " + resetEx);
            }
            
            UserInfoLock.readLock().unlock(); 
            OnlineUserLock.writeLock().unlock();
        }
    }
    
    public ArrayList<UserData> fetchUserLeaderboard() {   
        UserInfoLock.readLock().lock();
        ArrayList<UserData> leaderboard = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT username, wins, games, avgWinTime, dense_rank() OVER (ORDER BY wins desc) AS player_rank" +  
                " FROM UserInfo"
            );
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                leaderboard.add(new UserData(
                    rs.getString("username"),
                    rs.getInt("wins"),
                    rs.getInt("games"),
                    rs.getDouble("avgWinTime"),
                    rs.getInt("player_rank")
                ));
            }
        } catch (SQLException e) { 
            System.err.println("Error reading user info: " + e);
        } finally { UserInfoLock.readLock().unlock(); }
        
        return leaderboard;
    }

    public LogoutStatus removeOnlineUser(String username) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM " + ONL_TBL + " WHERE username = ?"
            ); stmt.setString(1, username);
            
            OnlineUserLock.writeLock().lock();
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 1) { return LogoutStatus.SUCCESS; }
            else { return LogoutStatus.SERVER_ERROR; }
        } catch (SQLException e) {
            System.err.println("Error removing online user: " + e);
            return LogoutStatus.SERVER_ERROR;
        } finally { OnlineUserLock.writeLock().unlock(); }
    }

    private void updateUserTable(
        String username, int wins, int games, double avgWinTime
    ) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + USR_TBL + 
                " SET wins = ?, games = ?, avgWinTime = ? WHERE username = ?"
            ); 
            
            stmt.setInt(1, wins);
            stmt.setInt(2, games);
            stmt.setDouble(3, avgWinTime);
            stmt.setString(4, username);
            
            UserInfoLock.writeLock().lock();
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user info: " + e);
        } finally { UserInfoLock.writeLock().unlock(); }
    }

    private void deleteUser(String username) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM " + USR_TBL + " WHERE username = ?"
            ); stmt.setString(1, username);
            
            UserInfoLock.writeLock().lock();
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e);
        } finally { UserInfoLock.writeLock().unlock(); }
    }
}
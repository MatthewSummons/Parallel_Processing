import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;


public class JDBCDemo {
	private static final String DB_HOST = "localhost";
	private static final String DB_USER = "c3358usr";
	private static final String DB_PASS = "c3358PASS";
	private static final String DB_NAME = "c3358";

	
	public static void main(String[] args) {
		try {
			new JDBCDemo().go();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {
			System.err.println("Connection failed: "+e);
		}
	}
	private Connection conn;
	public JDBCDemo() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		conn = DriverManager.getConnection("jdbc:mysql://" + 
			DB_HOST + "/"+ DB_NAME + "?user=" + DB_USER + "&password=" + DB_PASS
		);
		System.out.println("Database connection successful");
	}
	public void go() {
		
		Scanner keyboard = new Scanner(System.in);
		String line;
		System.out.print("> ");
		while(!(line = keyboard.next()).equals("exit")) {
			if(line.equals("create")) {
				insert(keyboard.next(), keyboard.next());
			} else if(line.equals("read")) {
				read(keyboard.next());
			} else if(line.equals("list")) {
				list();
			} else if(line.equals("update")) {
				update(keyboard.next(), keyboard.next());
			} else if(line.equals("delete")) {
				delete(keyboard.next());
			} else if (line.equals("month")) {
				try {
					list_month(keyboard.nextInt());
				} catch (InputMismatchException e) {
					System.out.println(
						"Invalid month! Please enter an integer between 01-12" 
					);
				}
			}
			System.out.print("> ");
		}
		keyboard.close();
	}
	private void insert(String name, String birthday) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO c3358_2025 (name, birthday) VALUES (?, ?)"
			);

			stmt.setString(1, name);
			stmt.setString(2, birthday);
			stmt.execute();
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Insert failed: "+e);
		}		
	}
	private void read(String name) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
				"SELECT birthday FROM c3358_2025 WHERE name= ?"
			);
			
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				System.out.println("Birthday of "+name+" is on "+rs.getDate(1).toString());
			} else {
				System.out.println(name+" not found!");
			}
		} catch (SQLException e) {
			System.err.println("Error reading record: "+e);
		}
	}
	private void list() {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT name, birthday FROM c3358_2025");
			while (rs.next()) {
				System.out.println("Birthday of " + rs.getString(1) + " is on " + rs.getDate(2).toString());
			}
		} catch (SQLException e) {
			System.err.println("Error listing records: " + e);
		}
	}
	private void update(String name, String birthday) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
				"UPDATE c3358_2025 SET birthday = ? WHERE name= ?"
			);
			stmt.setDate(1, java.sql.Date.valueOf(birthday));
			stmt.setString(2, name);
			int rows = stmt.executeUpdate();
			if(rows > 0) {
				System.out.println("Birthday of " + name + " updated");
			} else {
				System.out.println(name + " not found!");
			}
		} catch (SQLException e) {
			System.err.println("Error reading record: " + e);
		}	
	}
	private void delete(String name) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
				"DELETE FROM c3358_2025 WHERE name = ?"
			);
			stmt.setString(1, name);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				System.out.println("Record of " + name + " removed");
			} else {
				System.out.println(name + " not found!");
			}
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Error inserting record: " + e);
		}
	}
	private void list_month(int month) {		
		if (month < 1 || month > 12) {
			System.out.println(
				"Invalid month! Please enter a valid month between 01-12"
			);
			return;
		}
		
		try {
			PreparedStatement stmt = conn.prepareStatement(
				"SELECT name, birthday FROM c3358_2025 WHERE MONTH(birthday) = ?"
			);

			stmt.setInt(1, month);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				System.out.println(
					"Birthday of " + rs.getString(1) + " is on " + 
					rs.getDate(2).toString()
				);
			}

		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Error inserting record: " + e);
		}
	}
}
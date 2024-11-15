package chat_room;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	
	Connection conn;
	
	public Database() {
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:chatroom.db");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void createUserTable() throws SQLException {
		String tableSQL = "CREATE TABLE IF NOT EXISTS usernames (" +
						  "id INTEGER PRIMARY KEY, " +
						  "username TEXT NOT NULL UNIQUE, " +
						  "password TEXT NOT NULL, " +
						  "status TEXT, " +
						  "creation_time DATETIME DEFAULT CURRENT_TIMESTAMP);";
		Statement table = conn.createStatement();
		table.executeUpdate(tableSQL);
	}
	
	public void insertUsername(String username, String password) throws SQLException {
		String usernameSQL = "INSERT INTO usernames (username, password) VALUES (?, ?)";
		PreparedStatement usernameEntry = conn.prepareStatement(usernameSQL);
		usernameEntry.setString(1, username);
		usernameEntry.setString(1, password);
		usernameEntry.executeUpdate();
	}
	
	public boolean checkUser(String username) throws SQLException {
		String checkSQL = "SELECT * FROM usernames WHERE username=?";
		PreparedStatement check = conn.prepareStatement(checkSQL);
		check.setString(1, username);
		ResultSet result = check.executeQuery();
		
		if (result.next() && result.getInt(1) > 0) {
			return true;
		}
		
		return false;
	}
	
	public void connectionShutdown() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}

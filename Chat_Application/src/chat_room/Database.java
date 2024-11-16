package chat_room;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	
	private Connection conn;
	
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
	
	public void insertUser(String username, String password) throws SQLException {
		String usernameSQL = "INSERT INTO usernames (username, password) VALUES (?, ?);";
		PreparedStatement userEntry = conn.prepareStatement(usernameSQL);
		userEntry.setString(1, username);
		userEntry.setString(2, password);
		userEntry.executeUpdate();
	}
	
	public void updateStatus(String username, String status) throws SQLException {
		String statusSQL = "UPDATE usernames SET status=? WHERE username=?;";
		PreparedStatement statusEntry = conn.prepareStatement(statusSQL);
		statusEntry.setString(1, status);
		statusEntry.setString(2, username);
		statusEntry.executeUpdate();	
	}
	
	public boolean checkUsername(String username) throws SQLException {
		String checkSQL = "SELECT * FROM usernames WHERE username=?;";
		PreparedStatement check = conn.prepareStatement(checkSQL);
		check.setString(1, username);
		ResultSet result = check.executeQuery();
		
		if (result.next() && result.getInt(1) > 0) {
			return true;
		}
		
		return false;
	}
	
	public boolean checkPassword(String username, String password) throws SQLException {
		String checkSQL = "SELECT * FROM usernames WHERE username=? AND password=?;";
		PreparedStatement check = conn.prepareStatement(checkSQL);
		check.setString(1, username);
		check.setString(2, password);
		ResultSet result = check.executeQuery();
		
		if (result.next() && result.getInt(1) > 0) {
			return true;
		}
		
		return false;
	}
	
	public boolean checkStatus(String username, String status) throws SQLException {
		String checkSQL = "SELECT * FROM usernames WHERE username=? AND status=?;";
		PreparedStatement check = conn.prepareStatement(checkSQL);
		check.setString(1, username);
		check.setString(2, status);
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

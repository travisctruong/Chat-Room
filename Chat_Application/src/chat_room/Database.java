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
						  "creation_time DATETIME DEFAULT CURRENT_TIMESTAMP);";
		Statement table = conn.createStatement();
		table.executeUpdate(tableSQL);
	}
	
	public void insertUser(String username, String password) throws SQLException {
		String insertSQL = "INSERT INTO usernames (username, password) VALUES (?, ?);";
		PreparedStatement userEntry = conn.prepareStatement(insertSQL);
		userEntry.setString(1, username);
		userEntry.setString(2, password);
		userEntry.executeUpdate();
	}
	
	public void updateUsername(String username, String password, int id) throws SQLException {
		String updateSQL = "UPDATE usernames SET username=? WHERE password=? AND id=?;";
		PreparedStatement usernameEntry = conn.prepareStatement(updateSQL);
		usernameEntry.setString(1, username);
		usernameEntry.setString(2, password);
		usernameEntry.setInt(3, id);
		usernameEntry.executeUpdate();	
	}
	
	public void updatePassword(String username, String password) throws SQLException {
		String updateSQL = "UPDATE usernames SET password=? WHERE username=?;";
		PreparedStatement passwordEntry = conn.prepareStatement(updateSQL);
		passwordEntry.setString(1, password);
		passwordEntry.setString(2, username);
		passwordEntry.executeUpdate();	
	}
	
	public boolean checkUsername(String username) throws SQLException {
		String checkSQL = "SELECT * FROM usernames WHERE username=?;";
		PreparedStatement usernameEntry = conn.prepareStatement(checkSQL);
		usernameEntry.setString(1, username);
		
		ResultSet result = usernameEntry.executeQuery();
		return result.next() && result.getInt(1) > 0;
	}
	
	public boolean checkPassword(String username, String password) throws SQLException {
		String checkSQL = "SELECT * FROM usernames WHERE username=? AND password=?;";
		PreparedStatement passwordEntry = conn.prepareStatement(checkSQL);
		passwordEntry.setString(1, username);
		passwordEntry.setString(2, password);
		
		ResultSet result = passwordEntry.executeQuery();
		return result.next() && result.getInt(1) > 0;
	}
	
	public int getID(String username) throws SQLException {
		String getSQL = "SELECT id FROM usernames WHERE username=?;";
		PreparedStatement idEntry = conn.prepareStatement(getSQL);
		idEntry.setString(1, username);
		ResultSet result = idEntry.executeQuery();
		
		if (result.next()) {
            int id = result.getInt("id"); 
            return id;
        } 
		else {
            return -1;
        }
	}
	
	public void connectionShutdown() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}

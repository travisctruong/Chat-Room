package travistruong.chat_room;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

@Component
public class Server implements Runnable {
		
	private ArrayList<ClientHandler> connections;
	private ServerSocket server;
	private boolean flag;
	private ExecutorService pool;
	private Database database;
	
	private Server() {
		connections = new ArrayList<>();    // List of clients
		database = new Database();
	}

	@Override
	public void run() {                                  // Hosted on port 2424
		try {
			server = new ServerSocket(2424);
			pool = Executors.newCachedThreadPool();      // Thread pool handles each client automatically by allocating clients to available threads
			database.createUserTable();
			while (!flag) {
				Socket client = server.accept();                                 // Waits (blocking) to accept clients and answer client requests
				ClientHandler handler = new ClientHandler(this, client, database);
				pool.execute(handler);
				connections.add(handler);
			}
		} catch (IOException e) {
			serverShutdown();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	void broadcast(String message, int roomNum) {               // Sends message from client to all other clients
		for (ClientHandler handler : connections) {
			if (handler.roomGetter() == roomNum) {
				handler.receiveMessage(message);
			}
		}
	}
	
	HashMap<String, Integer> checkOnline() {
		HashMap<String, Integer> online = new HashMap<>();
		for (ClientHandler handler : connections) {
			if (handler.onlineGetter()) {
				online.put(handler.usernameGetter(), handler.roomGetter());
			}
		}
		return online;
	}
	
	ArrayList<String> checkInRoom(int roomNum) {
		ArrayList<String> inRoom = new ArrayList<>();
		for (ClientHandler handler : connections) {
			if (handler.roomGetter() == roomNum) {
				if (handler.onlineGetter()) {
					inRoom.add(handler.usernameGetter());
				}
			}
		}
		return inRoom;
	}
	
	void serverShutdown() {
		try {
			flag = true;
			pool.shutdown();
			server.close();
			for (ClientHandler handler : connections) {
				handler.clientShutdown();
			}
			database.connectionShutdown();
		} catch (IOException e) {}
	}
		
}
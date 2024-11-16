package chat_room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
				ClientHandler handler = new ClientHandler(client);
				pool.execute(handler);
				connections.add(handler);
			}
		} catch (IOException e) {
			serverShutdown();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void broadcast(String message, int roomNum) {               // Sends message from client to all other clients
		for (ClientHandler handler : connections) {
			if (handler.roomGetter() == roomNum) {
				handler.receiveMessage(message);
			}
		}
	}
	
	private void serverShutdown() {
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
	
	
	class ClientHandler implements Runnable {
		
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private int roomNum;
		
		private ClientHandler(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {     
				out = new PrintWriter(client.getOutputStream(), true);                     // IO between client and server
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				
				String username = null;
				String password;
				Boolean flag2 = true;
				while (flag2) {
					out.println("Enter username: ");                                         // Name initialization
					username = in.readLine();
					
					if (database.checkUsername(username)) {
						if (database.checkStatus(username, "offline")) {
							out.println("\nWelcome back " + username + ", please enter password: ");
							password = in.readLine();
							
							if (password.equals("/quit")) {
								clientShutdown();
								return;
							}
							if (!database.checkPassword(username, password)) {
								out.println("\nERROR: Incorrect password\n");
							}
							else {
								flag2 = false;
							}
						}
						else {
							out.println("\nERROR: Account in use\n");
						}
						
					}
					else {
						if (username.isEmpty() || username.contains(" ") || username.contains("\t") || username.contains("\n")) {
							out.println("\nERROR: Username cannot be blank or contain spaces\n");
						}
						else {
							out.println("\nEnter a password: ");
							password = in.readLine();
							if (password.equals("/quit")){
								clientShutdown();
								return;
							}
							database.insertUser(username, password);
							flag2 = false;
						}
					}
				}                          
				
				System.out.println(username + " connected");
				database.updateStatus(username, "online");
				roomNum = 1;
				broadcast("\n" + username + " has joined!\n", roomNum);
				out.println("Welcome! Use /help to display list of commands");
				String message;
				while ((message = in.readLine()) != null) {                 // Will wait (blocking) until message is received - continues running until buffered reader is closed
					String[] splitMessage = message.split(" ", 2);
					
					if (splitMessage[0].equals("/username")) {                                                        // Change name
						if (splitMessage.length == 2 && !splitMessage[1].isEmpty() && !splitMessage[1].contains(" ") && !splitMessage[1].contains("\t") && !splitMessage[1].contains("\n")) {
							broadcast("\n" + username + " changed username to " + splitMessage[1] + "\n", roomNum);
							System.out.println(username + " changed username to " + splitMessage[1]);
							username = splitMessage[1];
						}
						else {
							out.println("\nUsername cannot be blank or contain spaces\n");
						}
					}
					
					else if (splitMessage[0].equals("/quit")) {                                                       // Leave server
						if (splitMessage.length == 1) {
							broadcast("\n" + username + " left the room\n", roomNum);
							System.out.println(username + " has left the room");
							database.updateStatus(username, "offline");
							clientShutdown();
						}
						else {
							out.println("\nInvalid command\n");
						}
					}
					
					else if (splitMessage[0].equals("/join")) {                                                       // Change room
						if (splitMessage.length == 2) {
							try {
								int num = Integer.parseInt(splitMessage[1].trim());
								if (num > 0 && num < 100) {
									broadcast("\n" + username + " has entered room " + roomNum, roomNum);
									roomNum = num;
								}
								else {
									out.println("\nEnter a room number between 0-100\n");
								}
							} catch (NumberFormatException e) {
								out.println("\nEnter a room number between 0-100\n");
							}
							
						}
						else {
							out.println("\nEnter a room number between 0-100\n");
						}
					}
					
					else if (splitMessage[0].equals("/room")) {                                                     // Display room number 
						if (splitMessage.length == 1) {
							out.println("\nYou are in room " + roomNum);
						}
						else {
							out.println("\nInvalid command\n");
						}
					}
					
					else if (splitMessage[0].equals("/help")) {
						if (splitMessage.length == 1) {
							out.println("\n/username --- USAGE: /username {name}");
							out.println("/quit");
							out.println("/join --- USAGE: /join {room_number}");
							out.println("/room\n");
						}
						else {
							out.println("\nInvalid command\n");
						}
					}
					
					else if (splitMessage[0].startsWith("/")) { 													// Command attempt
						out.println("\nInvalid command\n");
					}
					
					else {                                                                                        // Send message to room
						broadcast(username + ": " + message, roomNum);
					}
				}
			} catch(IOException e) {
				clientShutdown();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
		
		private void receiveMessage(String message) {       // Get client messages
			out.println(message);
		}
		
		private void clientShutdown() {
			try {
				in.close();
				out.close();
				client.close();
			} catch (IOException e) {}
		}
		
		private int roomGetter() {
			return this.roomNum;
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}
}

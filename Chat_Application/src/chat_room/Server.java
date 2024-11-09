package chat_room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
	
	private ArrayList<ClientHandler> connections;
	private ServerSocket server;
	private boolean flag;
	private ExecutorService pool;
	
	public Server() {
		connections = new ArrayList<>();    // List of clients
	}

	@Override
	public void run() {                                  // Hosted on port 2424
		try {
			server = new ServerSocket(2424);
			pool = Executors.newCachedThreadPool();      // Thread pool handles each client automatically by allocating clients to available threads
			while (!flag) {
				Socket client = server.accept();                                 // Waits (blocking) to accept clients and answer client requests
				ClientHandler handler = new ClientHandler(client);
				pool.execute(handler);
				connections.add(handler);
			}
		} 
		
		catch (IOException e) {
			serverShutdown();
		}
	}
	
	public void broadcast(String message) {               // Sends message from client to all other clients
		for (ClientHandler handler : connections) {
			handler.receiveMessage(message);
		}
	}
	
	public void serverShutdown() {
		try {
			flag = true;
			pool.shutdown();
			server.close();
			for (ClientHandler handler : connections) {
				handler.clientShutdown();
			}
		} 
		
		catch (IOException e) {
			
		}
	}
	
	
	class ClientHandler implements Runnable {
		
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		
		public ClientHandler(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {     
				out = new PrintWriter(client.getOutputStream(), true);                     // IO between client and server
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				
				out.println("Enter a username: ");                                         // Name initialization
				String username = in.readLine();
				System.out.println(username);
				while (username.isEmpty() || username.contains(" ") || username.contains("\t") || username.contains("\n")) {
					out.println("\nUsername cannot be blank or contain spaces\n");
					username = in.readLine();
				}
				
				System.out.println(username + " connected");
				broadcast("\n" + username + " has joined!\n");
				String message;
				while ((message = in.readLine()) != null) {                 // Will wait (blocking) until message is received - continues running until buffered reader is closed
					String[] splitMessage = message.split(" ", 2);
					
					if (splitMessage[0].equals("/username")) {                                                        // Change name
						if (splitMessage.length == 2 && !splitMessage[1].isEmpty() && !splitMessage[1].contains(" ") && !splitMessage[1].contains("\t") && !splitMessage[1].contains("\n")) {
							broadcast("\n" + username + " changed username to " + splitMessage[1] + "\n");
							System.out.println(username + " changed username to " + splitMessage[1]);
							username = splitMessage[1];
						}
						else {
							out.println("\nUsername cannot be blank or contain spaces\n");
						}
					}
					
					else if (splitMessage[0].equals("/quit")) {                                                       // Leave server
						if (splitMessage.length == 1) {
							broadcast("\n" + username + " left the room\n");
							System.out.println(username + " has left the room");
							clientShutdown();
						}
						else {
							out.println("\nInvalid command\n");
						}
					}
					
					else if (splitMessage[0].startsWith("/")) { 													// Command attempt
						out.println("\nInvalid command\n");
					}
					
					else {                                                                                        // Send message to room
						broadcast(username + ": " + message);
					}
				}
				
			}
			
			catch(IOException e) {
				clientShutdown();
			}
		}
		
		public void receiveMessage(String message) {       // Get client messages
			out.println(message);
		}
		
		public void clientShutdown() {
			try {
				in.close();
				out.close();
				client.close();
			}
			
			catch (IOException e) {
				
			}
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}
}

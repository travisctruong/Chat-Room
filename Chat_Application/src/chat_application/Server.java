package chat_application;

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
	
	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean flag;
	private ExecutorService pool;
	
	public Server() {
		connections = new ArrayList<>();
		flag = false;
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(2424);
			pool = Executors.newCachedThreadPool();
			while (!flag) {
				Socket client = server.accept();
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler);
			}

		} catch (IOException e) {
			terminate();
		}
		
		
	}
	
	public void broadcast(String message) {
		for (ConnectionHandler ch : connections) {
			if (ch != null) {
				ch.sendMessage(message);
			}
		}
	}
	
	public void terminate() {
		try {
			flag = true;
			pool.shutdown();
			if (!server.isClosed()) {
				server.close();
			}
			for (ConnectionHandler ch : connections) {
				ch.terminate();
			}
		}
		catch (IOException e) {
			
		}
		
	}
	
	
	class ConnectionHandler implements Runnable {
		
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String username;
		
		public ConnectionHandler(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new PrintWriter(client.getOutputStream(), true);
				
				out.println("Enter a username: ");
				username = in.readLine();
//				while (username.isBlank()) {
//					out.println("Username required: ");
//					username = in.readLine();
//				}
				System.out.println(username + " connected");
				broadcast(username + " has joined!");
				String message;
				while ((message = in.readLine()) != null) {
					if (message.startsWith("/username")) {
						String[] splitMessage = message.split(" ", 2);
						if (splitMessage.length == 2) {
							broadcast(username + " changed username to " + splitMessage[1]);
							System.out.println(username + " changed username to " + splitMessage[1]);
							username = splitMessage[1];
							out.println("Username has successfully been changed to " + username);
						}
						else {
							out.println("No username was entered");
						}
					}
					else if (message.startsWith("/quit")) {
						broadcast(username + " left the chat");
						terminate();
					}
					else {
						broadcast(username + ": " + message);
					}
				}
				
			}
			
			catch(IOException e) {
				terminate();
			}
		}
		
		public void sendMessage(String message) {
			out.println(message);
		}
		
		public void terminate() {
			try {
				in.close();
				out.close();
				if (!client.isClosed()) {
					client.close();
				}
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

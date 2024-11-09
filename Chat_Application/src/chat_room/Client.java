package chat_room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private boolean done;
	
	@Override
	public void run() {
		try {
			client = new Socket("127.0.01", 2424);                   // Loop-back address
			out = new PrintWriter(client.getOutputStream(), true);                              // IO between client and server
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));            
			
			MessageHandler inHandler = new MessageHandler();            
			Thread thread = new Thread(inHandler);
			thread.start();
			
			String clientBroadcast;
			while ((clientBroadcast = in.readLine()) != null) {           // Will wait (block) until message is received - continues working until buffered reader is closed
				System.out.println(clientBroadcast);
			}
		} 
		
		catch (IOException e) {
			clientShutdown();
		}
	}
	
	public void clientShutdown() {
		done = true;
		try {
			in.close();
			out.close();
			client.close();
		} 
		
		catch (IOException e) {
			
		}
	}
	
	class MessageHandler implements Runnable {
		
		@Override
		public void run() {
			try {
				BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));    
				while (!done) {
					String clientMessage = standardInput.readLine();          // Message from standard input gets sent to server via "out"
					
					if (clientMessage.equals("/quit")) {            // Leave server
						out.println(clientMessage);
						standardInput.close();
						clientShutdown();
					}
					
					else {                                    // Send message to room
						out.println(clientMessage);
					}
				}
			} 
			
			catch (IOException e) {
				clientShutdown();
			}
		}
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		client.run();
	}
}

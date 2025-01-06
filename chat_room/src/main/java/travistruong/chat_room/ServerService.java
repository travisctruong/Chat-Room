package travistruong.chat_room;

import org.springframework.stereotype.Service;

@Service
public class ServerService {
	
	private Server server;
	
	private ServerService(Server server) {
		this.server = server;
	}
	
	void start() {
		server = new Server();
		server.run();
	}
	
	void shutdown() {
		server.serverShutdown();
	}
}
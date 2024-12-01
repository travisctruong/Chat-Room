package travistruong.chat_room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class ServerService {
	
	private final Server server;
	
	@Autowired
	private ServerService(Server server) {
		this.server = server;
	}
	
	@PostMapping
	void startServer() {
		server.run();
	}
	
	@PostMapping
	void stopServer() {
		server.serverShutdown();
	}
	
}

package travistruong.chat_room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/chat")
public class ServerController {
	
	private final ServerService serverService;
	
	@Autowired
	public ServerController(ServerService serverService) {
		this.serverService = serverService;
	}
	
	@PostMapping("/start")
	public ResponseEntity<String> startServer() {
		serverService.startServer();
		return ResponseEntity.ok("Server started.");
	}
	
	@PostMapping("/stop")
	public ResponseEntity<String> stopServer() {
		serverService.stopServer();
		return ResponseEntity.ok("Server stopped.");
	}
		
}

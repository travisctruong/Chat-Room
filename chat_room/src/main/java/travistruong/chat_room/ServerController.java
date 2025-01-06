package travistruong.chat_room;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ServerController {
	
	private final ServerService serverService;
	
	@Autowired
	private ServerController(ServerService serverService) {
		this.serverService = serverService;
	}
	
	@GetMapping("/start")
	private void start(HttpServletResponse response) throws IOException {
		response.getWriter().write("Server Started");
		serverService.start();
	}
	
	@GetMapping("/stop")
	private String stop() {
		serverService.shutdown();
		return "Server stopped";
	}
}
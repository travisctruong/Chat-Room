# Chat-Room
TCP Chat Room that allows devices to connect to a specific IP address and communicate with each other in real-time. Users can send and receive messages while connected to the server, enabling direct communication between multiple devices on the same network. Contains user authentication, user/chat commands, and chat room selection using SQLite for data storage.

Chat_Application --> Contains TCP Chat Room classes <br>
(Better) chat_room --> TCP Chat Room wrapped in Spring Boot, containing POST requests for starting/stopping server

How to use:
1. Clone repository
2. Run ChatRoomApplication.java
3. Submit POST request for starting server to localhost (http://localhost:8080/start)
4. Run Client.java on Command Line or Java IDE
5. Create username and password
6. Submit POST request for stopping server to localhost (http://localhost:8080/stop)

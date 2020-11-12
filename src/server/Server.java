package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8190;
    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("server started!");
        while(true){
            socket = server.accept();
            System.out.println("client connected! "+ socket.getRemoteSocketAddress());
            new ClientHandler(this, socket);
        }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Server closed!");
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void broadCastMsg(ClientHandler sender, String msg){
        String message = String.format("%s: %s", sender.getNickname(), msg);
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
        //
        public void broadCastSimpleMsg(ClientHandler sender,String recipient, String msg){
        String message = String.format("%s: %s", sender.getNickname(), msg);
            for (ClientHandler client : clients) {
                if(recipient.equals(client.getNickname())){
                    client.sendMessage(message);
                }
            }
        }
        //
        public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        }
        public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        }

    public AuthService getAuthService() {
        return authService;
    }
}
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private String login;


    public ClientHandler(Server server, Socket socket) {
        try {

            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //
                    try{
                        socket.setSoTimeout(120000);
                        // цикл аутентификации
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/")) {
                                if(str.startsWith("/reg ")){
                                    String[] token = str.split("\\s",4);
                                    boolean b = server.getAuthService().registration(token[1], token[2], token[3]);
                                    if(b){
                                        sendMessage("/regOk");
                                    } else {
                                        sendMessage("/regNo");
                                    }
                                }
                                if (str.startsWith("/auth ")) {
                                    String[] token = str.split("\\s",3);
                                    String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1], token[2]);
                                    if (newNick != null) {
                                        login = token[1];
                                        if(!server.isLoginAuthenticated(login)){
                                            nickname = newNick;
                                            out.writeUTF("/authOk " + nickname);
                                            server.subscribe(this);
                                            socket.setSoTimeout(0);
                                            break;
                                        } else{
                                            out.writeUTF("Учетная запись уже используется");
                                        }
                                    } else {
                                        out.writeUTF("Неверный логин/пароль");
                                    }
                                }
                            }
                        }

                        // цикл работы
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/")){

                                if (str.equals("/end")) {
                                    out.writeUTF("/end");
                                    break;
                                }
                                //
                                if(str.startsWith("/w")){
                                    String[] token = str.split("\\s+", 3);
                                    if(token.length < 3){
                                        continue;
                                    }
                                    server.SimpleMsg(this, token[1], token[2]);
                                }
                            } else{
                                server.broadCastMsg(this,str);

                            }
                            //
                        }
                    } catch(SocketTimeoutException e){
                        out.writeUTF("/end");
                    }
                    //
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnected!");
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (EOFException e){
                        System.out.println("End");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}

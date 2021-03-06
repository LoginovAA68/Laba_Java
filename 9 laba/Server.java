package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public Server() {
        ServerSocket server = null;
        Socket socket = null;
        try {
            server = new ServerSocket(8888); // открываем сокет на порту 8888
            System.out.println("Сервер запущен. Ожидаем клиентов...");
            while (true) {
                socket = server.accept(); // ждем подключения клиента
                System.out.println("Клиент подключился");
                new ClientHandler(socket); // после подключения клиента создаем объект класса ClientHandler, в котором создаем поток для каждого клиента
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

package ru.calc.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static volatile long START_TIME = System.currentTimeMillis();

    public static void main(String[] args) {
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

        /*new ChangeListener().start();
        new ChangeMaker().start();*/
    }

    static class ChangeListener extends Thread {
        private int i = 0;

        @Override
        public void run() {
            while (System.currentTimeMillis() - START_TIME < 10000) {
                try {
                    System.out.println((System.currentTimeMillis() - START_TIME) / 1000);
                    //System.out.println(START_TIME);
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("stop");
        }
    }

    static class ChangeMaker extends Thread {
        @Override
        public void run() {
            START_TIME = System.currentTimeMillis();


            try {
                Thread.sleep(2000);
                ServerSocket server = new ServerSocket(8189);
                System.out.println("Ожидание клиента");
                START_TIME = System.currentTimeMillis();
                Socket socket = server.accept();
                START_TIME = System.currentTimeMillis();
                System.out.println("Клиент подключился");

                Thread.sleep(3000);
                START_TIME = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
                /*try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

        }
    }
}

package ru.calc.client;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        LinkedList<String> list;
        try {
            // подключение к серверу
            Socket socket = new Socket("localhost", 8888);
            // Входной поток байт для обмена сообщениями
            DataInputStream in = new DataInputStream(socket.getInputStream());
            // Входной поток байт для получения LinkedList<String>
            ObjectInputStream inObj = new ObjectInputStream(socket.getInputStream());
            // Выходной поток байт для отправки сообщений на сервер
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            // Входной поток символов, читает символы с экрана
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String inStr = "";
            while(true) {
                // считывание одной строки
                inStr = reader.readLine();
                // отправка строки на сервер
                out.write(inStr.getBytes());
                // если введенная строка get_logs то ответ записывается в list (LinkedList<String>)
                if (inStr.equals("get_logs")) {
                    list = (LinkedList<String>) inObj.readObject();
                    // Вывод списка на экран
                    System.out.println(list);
                }
                else {
                    // дописывается символ перевода строки в конец сообщения, чтобы сервак понял что сообщение закончилось
                    out.write(10);
                    int x = -1;
                    String str = "";
                    // считывание символов из потока до тех пор пока не будет получен символ перевода каретки
                    while ((x = in.read()) != 13) {
                        str += (char) x;
                    }
                    // вывод результата вычислений полученного с сервера на экран
                    System.out.println(str);
                }
            }

        }
        catch (IOException | ClassNotFoundException e ) {
            System.out.println("Соединение разорвано");
        }
    }
}

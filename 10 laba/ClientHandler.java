package ru.calc.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private volatile long startTime;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            startTime = System.currentTimeMillis();
            // Для дополнительного задания добавили поток, который будет следить за временем простоя программы
            new Thread(() -> { // это лямбда-выражение, используется с функциональными интерфейсами, функциональный интерфейс это интерфейс с одним методом,
                // в данном случае public interface Runnable { public abstract void run(); }
                while (System.currentTimeMillis() - startTime < 1000 * 60) {}
                System.out.println("Время ожидания истекло");
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> { // это лямбда-выражение
                try {
                    while (true) {
                        // читаем первый байт из потока, если совпадает с первой буквой одной из наших команд
                        // пропускаем чтение потока дальше по команде
                        int x = in.read();
                        // после каждого прочтенного байта сбрасывается таймер
                        startTime = System.currentTimeMillis();
                        while (x != (byte)'a' && x != (byte)'s' && x != (byte)'m' && x != (byte)'d') {
                            x = in.read();
                            startTime = System.currentTimeMillis();
                        }
                        // создаем новый массив под команду в байтах
                        byte[] commandArr = new byte[8];
                        // записываем первый уже считанный байт в 31 строке
                        commandArr[0] = (byte) x;
                        // дописываем остальные байты команды
                        in.read(commandArr, 1, 7);
                        startTime = System.currentTimeMillis();
                        // создаем строку с командой
                        String command = new String(commandArr);
                        double a = 0, b = 0;
                        Double result = 8.0;
                        String strA = "";
                        String strB = "";

                        // проверяем что следующий символ не пробел (байт 32 - это пробел в кодировке utf8)
                        if ((x = in.read()) != 32) {
                            strA += (char) x;
                        }
                        startTime = System.currentTimeMillis();
                        // до тех пор пока не встретим следующий пробел (байт 32), или не прервется поток (-1), или не произойдет перевод строки (10 и 13)
                        // записываем символы их входного потока в строку
                        while ((x = in.read()) != 32 && x != -1 && x != 10 && x != 13) {
                            strA += (char) x;
                            startTime = System.currentTimeMillis();
                        }
                        while ((x = in.read()) != 32 && x != -1 && x != 10 && x != 13) {
                            strB += (char) x;
                            startTime = System.currentTimeMillis();
                        }
                        // этот блок try-catch нужен на случай некорректных данных в strA или strB, если что он поймает некорректные данный и вернет клиенту сообщение о том что формат неверен
                        try {
                            a = Double.parseDouble(strA);
                            b = Double.parseDouble(strB);
                            Calculator calculator = new Calculator();
                            switch (command) {
                                case "addition":
                                    result = calculator.add(a, b);
                                    break;
                                case "subtract":
                                    result = calculator.subs(a, b);
                                    break;
                                case "multipli":
                                    result = calculator.mult(a, b);
                                    break;
                                case "division":
                                    result = calculator.divide(a, b);
                                    break;
                                default:
                                    command = "Error format!";
                            }
                            if (command.equals("Error format!")) {
                                out.write(command.getBytes());
                            } else if (result == null) {
                                out.write(new String("inf").getBytes());
                            } else {
                                out.write(Double.toString(result).getBytes());
                            }
                        } catch (NumberFormatException e) {
                            out.write(new String("Error format!").getBytes());
                        }
                        // дописываем в конец любого сообщения перевод строки (10) и возврат каретки(13)
                        out.write(10);
                        out.write(13);
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
                System.out.println("Клиент отключен");
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

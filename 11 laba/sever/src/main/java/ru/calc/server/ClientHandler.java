package ru.calc.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ClientHandler {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private ObjectOutputStream outObj;
    private volatile long startTime;
    private Commands command;

    enum Commands {
        EMPTY(""), ADD("addition"), SUB("subtract"), MUL("multipli"), DIV("division"), GET_LOGS("get_logs");
        private String command;

        Commands(String command) {
            this.command = command;
        }

        public static Commands getMsgTypeByString(String str) {
            for (Commands c : Commands.values()) {
                if (c.command.equals(str))
                    return c;
            }
            return EMPTY;
        }

        public String getCommand() {
            return command;
        }
    }

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.outObj = new ObjectOutputStream(socket.getOutputStream());
            startTime = System.currentTimeMillis();
            // Для дополнительного задания добавили поток, который будет следить за временем простоя программы
            new Thread(() -> { // это лямбда-выражение, используется с функциональными интерфейсами, функциональный интерфейс это интерфейс с одним методом,
                // в данном случае public interface Runnable { public abstract void run(); }
                while (System.currentTimeMillis() - startTime < 1000 * 60) {
                }
                System.out.println("Время ожидания истекло");
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> { // это лямбда-выражение
                try {
                    LinkedList<String> listCommand = new LinkedList<>();
                    while (true) {
                        // читаем первый байт из потока, если совпадает с первой буквой одной из наших команд
                        // пропускаем чтение потока дальше по команде
                        int x = in.read();
                        // после каждого прочтенного байта сбрасывается таймер
                        startTime = System.currentTimeMillis();
                        while (x != (byte) 'a' && x != (byte) 's' && x != (byte) 'm' && x != (byte) 'd' && x != (byte) 'g') {
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
                        String commandStr = new String(commandArr);
                        // после формирования команды проверяем ее на соответствие протоколу (enum Commands)
                        command = Commands.getMsgTypeByString(commandStr);
                        // Если пришла команда get_logs то отправляем сериализованный список LinkedList<String> клиенту
                        if (command == Commands.GET_LOGS) {
                            outObj.writeObject(listCommand);
                        } else {
                            // Проверяем, что полученное сообщение соответствует формату протокола (enum Commands)
                            if (command == Commands.EMPTY) {
                                out.writeBytes("Error format!");
                            } else {
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
                                    String res = "";
                                    switch (command) {
                                        case ADD:
                                            result = calculator.add(a, b);
                                            break;
                                        case SUB:
                                            result = calculator.subs(a, b);
                                            break;
                                        case MUL:
                                            result = calculator.mult(a, b);
                                            break;
                                        case DIV:
                                            result = calculator.divide(a, b);
                                            break;
                                    }
                                    // добавляем в список каждую корректную команду математического действия
                                    listCommand.push(command.getCommand());
                                    if (result == null) {
                                        out.write("inf".getBytes());
                                    } else {
                                        out.write(Double.toString(result).getBytes());
                                    }
                                } catch (NumberFormatException e) {
                                    out.write("Error format!".getBytes());
                                }

                            }
                            // дописываем в конец любого сообщения (кроме списка LinkedList) перевод строки (10) и возврат каретки(13)
                            out.write(10);
                            out.write(13);
                            out.flush();
                        }
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

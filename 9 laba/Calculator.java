package server;

public class Calculator {
    synchronized public static double add(double a, double b) {
        return a + b;
    }
    synchronized public static double subs(double a, double b) {
        return a - b;
    }
    synchronized public static double mult(double a, double b) {
        return a * b;
    }
    synchronized public static Double divide(double a, double b) {
        if (b != 0)
            return a / b;
        return null;
    }
}

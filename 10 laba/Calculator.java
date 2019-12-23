package ru.calc.server;

public class Calculator {
    synchronized public double add(double a, double b) {
        return a + b;
    }
    synchronized public double subs(double a, double b) {
        return a - b;
    }
    synchronized public double mult(double a, double b) {
        return a * b;
    }
    synchronized public Double divide(double a, double b) {
        if (b != 0)
            return a / b;
        return null;
    }
}

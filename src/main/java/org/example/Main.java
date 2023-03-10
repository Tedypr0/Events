package org.example;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Helper helper = new Helper();
        helper.eventCreation();
        helper.threadCreation();
        Thread.sleep(100);
        System.out.println(helper.counter.get());
    }
}
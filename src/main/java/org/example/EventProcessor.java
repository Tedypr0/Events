package org.example;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventProcessor extends Thread {
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> eventsQueues;

    public EventProcessor(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> eventsQueues) {
        this.isPoisonFound = isPoisonFound;
        this.eventsQueues = eventsQueues;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            Queue<Event> events;
            try {
                events = eventsQueues.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (events.peek() != null) {
                if (events.peek().getMessage().equals(Helper.POISON_MESSAGE)) {
                    isPoisonFound.set(true);
                }
                Event event;
                while (!events.isEmpty()) {
                    event = events.poll();
                    if (!event.getMessage().equals(Helper.POISON_MESSAGE)) {
                        System.out.printf("%s works with Event %s%n", this.getName(), event.getMessage());
                    }
                }
            }
        }
    }
}

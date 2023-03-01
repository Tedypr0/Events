package org.example;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventProcessor extends Thread {
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> eventsQueues;
    private final Map<Integer, Queue<Event>> refKeeper;

    public EventProcessor(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> eventsQueues, Map<Integer, Queue<Event>> refKeeper) {
        this.isPoisonFound = isPoisonFound;
        this.eventsQueues = eventsQueues;
        this.refKeeper = refKeeper;
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
                } else {
                    Event event = null;
                    while (!events.isEmpty()) {
                        event = events.poll();
                    //    System.out.printf("%s works with Event %s%n", this.getName(), event.getMessage());
                    }
                    assert event != null;
                    refKeeper.remove(event.hashCode());
                }
            }
        }
    }
}
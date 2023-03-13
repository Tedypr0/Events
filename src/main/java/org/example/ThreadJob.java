package org.example;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadJob implements Runnable {
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> eventsQueues;
    private final AtomicInteger counter;

    public ThreadJob(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> eventsQueues,
                     AtomicInteger counter) {
        this.isPoisonFound = isPoisonFound;
        this.eventsQueues = eventsQueues;
        this.counter = counter;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            Queue<Event> events;
            Event lastEvent;
            try {
                events = eventsQueues.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (events.peek() != null) {
                if (events.peek().getMessage().equals(Helper.POISON_MESSAGE)) {
                    isPoisonFound.set(true);
                } else {
                    while (!events.isEmpty()) {
                        lastEvent = events.poll();
                        counter.getAndIncrement();
                        eventsQueues.removeQueueFromRefKeeper(lastEvent);
                    }
                }
            }
        }
    }
}

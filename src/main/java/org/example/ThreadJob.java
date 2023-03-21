package org.example;

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
            EventsQueue<Event> events;
            Event lastEvent = null;
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
                    }
                    /*
                     * Will try to remove Queue from refKeeper, when the current thread exists from while loop.
                     * There is a secondary isEmpty check that checks if the queue is empty. It's also synchronized, so
                     * no problems should be expected.
                     */
                    eventsQueues.removeQueueFromRefKeeper(lastEvent.getId());
                }
            }
        }
    }
}

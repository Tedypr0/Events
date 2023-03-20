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
                    /*
                     * Will poll events, until events Queue is empty.
                     * Main thread could add a new Event, before the completion of events.isEmpty().
                     * Upon next invocation of isEmpty() should return false and continue polling.
                     */
                    while (!events.isEmpty()) {
                        lastEvent = events.poll();
                        counter.getAndIncrement();
                        /*
                         * If the current thread reaches here and by then main thread has added a new Event,
                         * when we go to the method (removeFromRefKeeper), there is an isEmpty check and not remove the new
                         * Event and lose it. After that continue as expected. (Check isEmpty poll ..etc..)
                         */
                        eventsQueues.removeQueueFromRefKeeper(lastEvent.getId());
                    }
                }
            }
        }
    }
}

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
            try {
                events = eventsQueues.poll();
                if(events == null){
                    System.out.println("?");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (events.peek() != null) {
                if (events.peek().getMessage().equals(Helper.POISON_MESSAGE)) {
                    isPoisonFound.set(true);
                } else {
                    pollElements(events);
                    /*
                     * Will try to remove Queue from refKeeper, when the current thread exists from while loop.
                     * There is a secondary isEmpty check that checks if the queue is empty. It's also synchronized, so
                     * no problems should be expected.
                     *
                     * Problem is that if this method does not remove queue from refKeeper (which it won't if there is at least 1 event in it),
                     * we won't be able to process these events again, hey will be kept in refKeeper, but not in "Master" queue, from where Threads will poll queues.
                     */

                }
            }
        }
    }

    private void pollElements(EventsQueue<Event> eventsQueue) {
        Event lastEvent = null;
        while (!eventsQueue.isEmpty()) {
            lastEvent = eventsQueue.poll();
            counter.getAndIncrement();
        }
        assert lastEvent != null;
        if (eventsQueues.removeQueueFromRefKeeper(lastEvent.getId())) {
            pollElements(eventsQueue);
        }
    }
}

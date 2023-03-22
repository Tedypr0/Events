package org.example;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadJob extends Thread{
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> eventsQueues;
    private final AtomicInteger counter;
    private final AtomicBoolean isQueueFinished;

    public ThreadJob(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> eventsQueues,
                     AtomicInteger counter, AtomicBoolean isQueueFinished) {
        this.isPoisonFound = isPoisonFound;
        this.eventsQueues = eventsQueues;
        this.counter = counter;
        this.isQueueFinished = isQueueFinished;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            EventsQueue<Event> events;
            try {
                events = eventsQueues.poll();
                isQueueFinished.set(false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (events.peek() != null) {
                if (events.peek().getMessage().equals(Helper.POISON_MESSAGE)) {
                    isPoisonFound.set(true);
                } else {
                    Event lastEvent;
                    /* Check if we have removed the current Queue from refKeeper.
                     * False: we still have events in current Queue and refKeeper contains a reference to it.
                     * True: we did not have any Events at the time in the Queue and by invoking removeQueueFromRefKeeper
                     * we check in the secondary synchronized method, if the queue is still empty and if it is, we set
                     * remove the queue from refKeeper and set isQueueFinished to true, so we can stop this while loop.
                     */
                    while (!isQueueFinished.get()) {
                        lastEvent = events.poll();
                        counter.getAndIncrement();
                        if(events.isEmpty()){
                            eventsQueues.removeQueueFromRefKeeper(lastEvent.getId(), isQueueFinished);
                        }
                    }
                }
            }
        }
    }
}

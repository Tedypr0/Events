package org.example;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class UniqueEventsQueue<T> {
    //Storage
    private final Queue<EventsQueue<T>> eventsQueues;

    //Keep references to Queues if we need to add new Events with existing keys and lookup.
    private final Map<Integer, EventsQueue<T>> refKeeper;

    public UniqueEventsQueue(Queue<EventsQueue<T>> eventsQueues, Map<Integer, EventsQueue<T>> refKeeper) {
        this.eventsQueues = eventsQueues;
        this.refKeeper = refKeeper;
    }

    public void add(int key, T element) {
        if (key == Integer.MAX_VALUE) {
            EventsQueue<T> poisonousQueue = new EventsQueue<>(refKeeper);
            poisonousQueue.add(element);
            eventsQueues.add(poisonousQueue);
            return;
        }

        /*
         * Synchronized checking if refKeeper contains a reference to a Queue. If it does not, create a new Queue, add
         * an Event to it and put it in refKeeper.
         *
         * If it does contain Event with this key (reference), we can add it directly
         * to the desired Queue. This synchronization is done in the Queue itself (EventsQueue), thus improving performance.
         */

        synchronized (this) {
            if (!refKeeper.containsKey(key)) {
                EventsQueue<T> newQueue = new EventsQueue<>(refKeeper);
                newQueue.add(element);
                refKeeper.put(key, newQueue);
                eventsQueues.add(newQueue);
                notify();
                return;
            }

            EventsQueue<T> events = refKeeper.get(key);
            events.add(element);
        }
    }

    public synchronized EventsQueue<T> poll() throws InterruptedException {
        while (eventsQueues.isEmpty()) {
            wait();
        }

        EventsQueue<T> queue = eventsQueues.peek();
        T peekedEvent = queue.peek();
        // This if statements is for not removing the Queue containing poisonPill Event from eventsQueues.
        if (peekedEvent != null && ((Event) peekedEvent).getId() == Integer.MAX_VALUE) {
            notify();
            return queue;
        }

        notify();
        return eventsQueues.poll();
    }

    public synchronized void removeQueueFromRefKeeper(int ref, AtomicBoolean isQueueFinished) {
        // Remove Queue from EventsQueue
            EventsQueue<T> queue = refKeeper.get(ref);
            queue.removeQueueFromRefKeeper(ref, isQueueFinished);
    }
}
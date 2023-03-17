package org.example;

import java.util.Map;
import java.util.Queue;

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

        /* We need to synchronize if/else statement, because if two threads with the same key check if their key is
         * contained in the refKeeper, and it's false, they both will continue to the else and create two new Queues
         * instead of creating one queue and adding an element to it.
         */
        synchronized (this) {
            if (refKeeper.containsKey(key)) {
                EventsQueue<T> events = refKeeper.get(key);
                events.add(element);
            } else {

                EventsQueue<T> newQueue = new EventsQueue<>(refKeeper);
                newQueue.add(element);
                refKeeper.put(key, newQueue);
                eventsQueues.add(newQueue);
                notify();
            }
        }
    }

    public synchronized EventsQueue<T> poll() throws InterruptedException {
        while (eventsQueues.isEmpty()) {
            wait();
        }

        EventsQueue<T> queue = eventsQueues.peek();

        // This if statements is for not removing the Queue containing poisonPill Event from eventsQueues.
        if (queue.peek() != null && ((Event) queue.peek()).getId() == Integer.MAX_VALUE) {
            notify();
            return queue;
        }

        notify();
        return eventsQueues.poll();
    }

    /*
     * Removing QueueFromRefKeeper, which here isn't synchronized, but synchronization is done for every Queue we want to remove from refKeeper.
     */
    public void removeQueueFromRefKeeper(int ref) {
        // Remove Queue from EventsQueue
            refKeeper.get(ref).removeQueueFromRefKeeper(ref);
    }
}

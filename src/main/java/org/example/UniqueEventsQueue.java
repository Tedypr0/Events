package org.example;

import java.util.Map;
import java.util.Queue;

public class UniqueEventsQueue<T> {
    //Storage
    private final Queue<EventsQueue<T>> eventsQueues;

    //Keep references to Queues if we need to add new Events with existing keys.
    private final Map<Integer, EventsQueue<T>> refKeeper;

    public UniqueEventsQueue(Queue<EventsQueue<T>> eventsQueues, Map<Integer, EventsQueue<T>> refKeeper) {
        this.eventsQueues = eventsQueues;
        this.refKeeper = refKeeper;
    }

    public synchronized void add(int key, T element) {
        if (key == Integer.MAX_VALUE) {
            EventsQueue<T> poisonousQueue = new EventsQueue<>(refKeeper);
            poisonousQueue.add(element);
            eventsQueues.add(poisonousQueue);
            notify();
            return;
        }
        if (refKeeper.containsKey(key)) {
            refKeeper.get(key).add(element);
        } else {
            EventsQueue<T> newQueue = new EventsQueue<>(refKeeper);
            newQueue.add(element);
            refKeeper.put(key, newQueue);
            eventsQueues.add(newQueue);
        }
        notify();
    }

    public synchronized EventsQueue<T> poll() throws InterruptedException {
        while (eventsQueues.isEmpty()) {
            wait();
        }

        EventsQueue<T> queue = eventsQueues.peek();

        if (queue.peek() != null && queue.peek().hashCode() == Integer.MAX_VALUE) {
            notify();
            return queue;
        }

        notify();
        return eventsQueues.poll();
    }

    /*
     * This has to be inside a queue add method, so we don't lock adding events to other queues.
     */
    public void removeQueueFromRefKeeper(int ref){
        // Remove Queue from EventsQueue
        refKeeper.get(ref).removeQueueFromRefKeeper(ref);
    }
}

package org.example;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventsQueue<T> {

    Queue<T> queue = new ConcurrentLinkedQueue<>();
    private final Map<Integer, EventsQueue<T>> refKeeper;

    public EventsQueue(Map<Integer, EventsQueue<T>> refKeeper) {
        this.refKeeper = refKeeper;
    }

    public synchronized void add(T event) {
        queue.add(event);
    }

    public synchronized T poll() {
        return queue.poll();
    }

    public synchronized T peek() {
        return queue.peek();
    }

    // Needs to be synchronized, because main thread could add an Event and isEmpty, could return true.
    public synchronized boolean isEmpty(){
        return queue.isEmpty();
    }

    // Allows us to remove queues from refKeeper map, without stopping the addition of new elements to other queues.
    // Synchronization of only the current queue.
    public synchronized void removeQueueFromRefKeeper(int ref){
        if(queue.isEmpty()){
            refKeeper.remove(ref);
        }
    }
}

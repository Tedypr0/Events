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

    //
    public synchronized void removeQueueFromRefKeeper(Integer ref){
        if(queue.isEmpty()){
            refKeeper.remove(ref);
        }
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }
}

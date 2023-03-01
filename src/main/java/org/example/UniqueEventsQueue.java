package org.example;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UniqueEventsQueue<T> {
    //Storage
    private final Queue<Queue<T>> eventsQueues;

    //Keep references to Queues if we need to add new Events with existing keys.
    private final Map<Integer, Queue<T>> refKeeper;

    public UniqueEventsQueue(Queue<Queue<T>> eventsQueues, Map<Integer, Queue<T>> refKeeper) {
        this.eventsQueues = eventsQueues;
        this.refKeeper = refKeeper;
    }

    public synchronized void add(T element){
        if(element.hashCode() == Integer.MAX_VALUE){
            Queue<T> poisonousQueue = new ConcurrentLinkedQueue<>();
            poisonousQueue.add(element);
            eventsQueues.add(poisonousQueue);
            notify();
            return;
        }
        if(refKeeper.containsKey(element.hashCode())){
            refKeeper.get(element.hashCode()).add(element);
        }else{
            Queue<T> newQueue = new ConcurrentLinkedQueue<>();
            newQueue.add(element);
            refKeeper.put(element.hashCode(), newQueue);
            eventsQueues.add(newQueue);
        }
        notify();
    }

    public synchronized Queue<T> poll() throws InterruptedException{
        while(eventsQueues.isEmpty()){
            wait();
        }

        Queue<T> queue = eventsQueues.peek();

        if(queue.peek() != null && queue.peek().hashCode() == Integer.MAX_VALUE){
            notify();
            return queue;
        }

        notify();
        return eventsQueues.poll();
    }
}

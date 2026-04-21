package synchronization;

import java.util.LinkedList;

import synchronization.ProducerConsumer.Consumer;
import synchronization.ProducerConsumer.Producer;

public class ProducerConsumerSafe {
    static int consumedCount;
    static final Object lock = new Object();
    static LinkedList<Integer> q = new LinkedList<Integer>();

    static class Producer implements Runnable {
        int id;
        long count;

        public Producer(int id, long count) {
            this.id = id;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                synchronized (lock) {
                    q.addLast(i);
                }
            }
        }
    }

    static class Consumer implements Runnable {
        int id;
        long count;

        public Consumer(int id, long count) {
            this.id = id;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                synchronized (lock) {
                    Integer num = q.getFirst();
                    // consumedCount++;
                }
            }
        }
    }

    public static void main(String[] args) {
        int T = (args.length >= 1 ? Integer.parseInt(args[0]) : 2);
        long N = (args.length >= 2 ? Long.parseLong(args[1]) : 10000000);

        Thread[] producers = new Thread[T];
        Thread[] consumers = new Thread[T];

        for (int i = 0; i < T; i++) {
            producers[i] = new Thread(new Producer(i, N));
            consumers[i] = new Thread(new Consumer(i, N));
        }
        long time = System.currentTimeMillis();
        // Start threads
        for (int i = 0; i < T; i++) {
            producers[i].start();
            consumers[i].start();
        }

        // Wait for threads completion
        for (int i = 0; i < T; i++) {
            try {
                producers[i].join();
                consumers[i].join();
            } catch (InterruptedException e) {
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println(T + ": consumed " + consumedCount + " elements " + " in " + time + " ms");
    }
}

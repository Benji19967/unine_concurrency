package synchronization;

import java.util.concurrent.ArrayBlockingQueue;

public class ProducerConsumer {

    static int consumedCount;
    static final Object lock = new Object();
    static ArrayBlockingQueue<Integer> buffer = new ArrayBlockingQueue<>(1000);

    static class Producer implements Runnable {
        int id;
        int count;

        public Producer(int id, int count) {
            this.id = id;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                try {
                    buffer.put(i);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class Consumer implements Runnable {
        int id;
        int count;

        public Consumer(int id, int count) {
            this.id = id;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < count; i++) {
                    Integer num = buffer.take();
                    synchronized (lock) {
                        consumedCount++;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        for (int t : new int[] { 1, 2, 4, 8, 16 }) {
            int n = 100_000;
            int numElementsPerThread = n / t;
            Thread[] producers = new Thread[t];
            Thread[] consumers = new Thread[t];

            for (int i = 0; i < t; i++) {
                producers[i] = new Thread(new Producer(i, numElementsPerThread));
                consumers[i] = new Thread(new Consumer(i, numElementsPerThread));
            }
            long time = System.currentTimeMillis();
            // Start threads
            for (int i = 0; i < t; i++) {
                producers[i].start();
                consumers[i].start();
            }

            // Wait for threads completion
            for (int i = 0; i < t; i++) {
                try {
                    producers[i].join();
                    consumers[i].join();
                } catch (InterruptedException e) {
                }
            }
            time = System.currentTimeMillis() - time;
            System.out.println(t + ": consumed " + consumedCount + " elements " + " in " + time + " ms");
        }
    }
}

package synchronization;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import synchronization.ProducerConsumer.Consumer;
import synchronization.ProducerConsumer.Producer;

public class ProducerConsumerBarriersLatches {
    static final int T = 2;
    static final int N = 10000000;
    static int consumedCount;
    static final Object lock = new Object();
    static ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<Integer>();
    static CyclicBarrier barrier = new CyclicBarrier(T);
    static CountDownLatch latch = new CountDownLatch(T);

    static class Producer implements Runnable {
        int id;
        long count;

        public Producer(int id, long count) {
            this.id = id;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                barrier.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < count; i++) {
                q.add(i);
                // System.out.println(i);
            }
            latch.countDown();
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
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < count; i++) {
                Integer num = q.poll();
                // synchronized (lock) {
                // consumedCount++;
                // System.out.println(consumedCount);
                // }
            }
        }
    }

    public static void main(String[] args) {

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

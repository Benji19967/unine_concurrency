package synchronization;

import java.util.concurrent.locks.ReentrantLock;

// Implementation of solution 4 (see DiningPhilosophers.java)
// Guaranteed starvation free (and deadlock free).
// Drawback: one thread ends up doing 90% of the work.

public class DiningPhilosophersNoStarvation {
    static int eat_counter = 0;
    static int num_philosophers = 20;
    static ReentrantLock[] locks;
    static int DONE_EAT_COUNTER = 10000;

    static class Philosopher implements Runnable {
        int id;
        int num_philosophers;
        int thread_eat_counter;

        public Philosopher(int id, int num_philosophers) {
            this.id = id;
            this.num_philosophers = num_philosophers;
            this.thread_eat_counter = 0;
        }

        @Override
        public void run() {
            while (true) {
                // Give other philosophers a chance to acquire the lock,
                // don't hog the CPU. This enables fairness. But also makes the code
                // significantly slower.
                think();

                if (eat_counter >= DONE_EAT_COUNTER) {
                    System.out.println(this.id + " " + thread_eat_counter);
                    return;
                }

                int first_lock_idx;
                int second_lock_idx;
                if (this.id == num_philosophers - 1) {
                    first_lock_idx = (this.id + 1) % this.num_philosophers; // right
                    second_lock_idx = this.id; // left
                } else {
                    first_lock_idx = this.id; // left
                    second_lock_idx = (this.id + 1) % this.num_philosophers; // right
                }

                locks[first_lock_idx].lock();
                locks[second_lock_idx].lock();

                // Start critical section
                eat_counter++;
                thread_eat_counter++;
                // End critical section

                locks[second_lock_idx].unlock();
                locks[first_lock_idx].unlock();
            }
        }

        private void think() {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        locks = new ReentrantLock[num_philosophers];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock(true);
        }

        System.out.println("Start with " + num_philosophers + " philosophers");
        // Create threads
        Thread[] threads = new Thread[num_philosophers];
        for (int i = 0; i < num_philosophers; i++) {
            threads[i] = new Thread(new Philosopher(i, num_philosophers));
        }
        long time = System.currentTimeMillis();
        // Start threads
        for (int i = 0; i < num_philosophers; i++) {
            threads[i].start();
        }
        // Wait for threads completion
        for (int i = 0; i < num_philosophers; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Finished eating " + eat_counter + " times in " + time);
    }
}
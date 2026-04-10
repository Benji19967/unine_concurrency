package synchronization;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/* 
Let's think of a few possible solutions. 

0.) Try to lock fork on the left, and then try to lock fork on the right
    Drawback: Deadlock can occur (e.g. all philosophers hold the lock on their left fork)

1.) Have a queue, or ticketing system, and make one philosopher eat a time.
    Drawback: No concurrency
    
2.) Create two groups of non-adjacent philosophers (e.g. 1,3,5,... and 0,2,4,...), and make each 
    eat alternatively.
    Drawback: Assume there is one slow eater per group, then it will block all other philosophers
    from eating more frequently.

3.) Use a fair lock and tryLock() for second fork/lock with timeout. If you can't acquire the second fork/lock within
    some timeframe, release the first lock. Avoids deadlock. Not guaranteed starvation free, but low 
    probability of starvation.
    
4.) Break symmetry: pick one philosopher that picks up right fork first. If using fair locks (but not tryLock()), 
    this is guaranteed starvation free.
    Drawback: Parallelism not fully maxed out (better: don't hold a fork/lock unless left and right are free
    -->more complex algorithm)
*/

// Implementation of solution 3 
// It happens once every few runs that one thread never eats when DONE_EAT_COUNTER = 1000.
// This gets increasingly rarer as DONE_EAT_COUNTER gets larger.

public class DiningPhilosophers {
    static int eat_counter = 0;
    static int num_philosophers = 20;
    static ReentrantLock[] locks;
    static int DONE_EAT_COUNTER = 100000;

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
                if (eat_counter >= DONE_EAT_COUNTER) {
                    System.out.println(this.id + " " + thread_eat_counter);
                    return;
                }
                // Grab left
                locks[this.id].lock();
                try {
                    // Try to grab right
                    int right_fork_idx = (this.id + 1) % this.num_philosophers;
                    if (locks[right_fork_idx].tryLock(50, TimeUnit.MILLISECONDS)) {
                        eat_counter++;
                        thread_eat_counter++;
                        locks[right_fork_idx].unlock();

                    }
                    locks[this.id].unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
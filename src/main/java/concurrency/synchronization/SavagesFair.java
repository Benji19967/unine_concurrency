package synchronization;

import java.util.concurrent.atomic.AtomicIntegerArray;

/*
Use a ticketing system--each savage, and the cook, wait until it's their turn. 

This assures fairness and that no thread is starving.
*/

public class SavagesFair {
    static int N = 10; // pot capacity
    static volatile int pot_counter = N;
    static volatile int total_portions_eaten = 0;
    static volatile boolean is_refilling = false;
    static ReadWriteLock lock = new ReadWriteLock();
    static AtomicIntegerArray ticket_and_current = new AtomicIntegerArray(2);

    static class Savage implements Runnable {
        int id;
        int num_savages;
        int num_times_eaten = 0;

        public Savage(int id, int num_savages) {
            this.id = id;
            this.num_savages = num_savages;
        }

        @Override
        public void run() {
            while (true) {
                // Ticketing system to assure fairness
                int ticket_number = ticket_and_current.getAndIncrement(0);

                // Busy-wait until it is this thread's turn
                while (ticket_and_current.get(1) != ticket_number) {
                    continue;
                }

                // Start critical section
                // System.out.println(id + " start");
                if (total_portions_eaten >= 5 * num_savages) {
                    ticket_and_current.getAndIncrement(1);
                    return;
                }
                if (!is_refilling && pot_counter > 0) {
                    if (Math.floorDiv(total_portions_eaten, num_savages) <= num_times_eaten) {
                        System.out.println(id + " " + total_portions_eaten);
                        pot_counter--;
                        total_portions_eaten++;
                        num_times_eaten++;
                    }
                } else if (pot_counter == 0 && !is_refilling)
                    is_refilling = true;
                // System.out.println(id + " end");
                ticket_and_current.getAndIncrement(1);
                // End critical section
            }
        }
    }

    static class Cook implements Runnable {
        int num_savages;

        public Cook(int num_savages) {
            this.num_savages = num_savages;
        }

        @Override
        public void run() {
            while (true) {
                while (!is_refilling && total_portions_eaten < 5 * num_savages) {
                    continue;
                }

                // Ticketing system
                int ticket_number = ticket_and_current.getAndIncrement(0);
                int current = ticket_and_current.get(1);

                // Busy-wait until it is this thread's turn
                while (!(ticket_number == current)) {
                    current = ticket_and_current.get(1);
                    continue;
                }

                // Start critical section
                // Done if all savages have eaten 5 times
                if (total_portions_eaten >= 5 * num_savages)
                    return;
                if (is_refilling) {
                    pot_counter = N;
                    is_refilling = false;
                }
                ticket_and_current.getAndIncrement(1);
                // End critical section
            }
        }
    }

    public static void main(String[] args) {
        int num_savages = (args.length >= 1 ? Integer.parseInt(args[0]) : 20);
        int num_threads = num_savages + 1; // cook

        System.out.println("Start with " + num_savages + " savages and 1 cook");
        // Create threads
        Thread[] threads = new Thread[num_threads];
        for (int i = 0; i < num_savages; i++) {
            threads[i] = new Thread(new Savage(i, num_savages));
        }
        threads[num_threads - 1] = new Thread(new Cook(num_savages));

        long time = System.currentTimeMillis();
        // Start threads
        for (int i = 0; i < num_threads; i++) {
            threads[i].start();
        }
        // Wait for threads completion
        for (int i = 0; i < num_threads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println(
                num_savages + " savages have eaten a total of " + total_portions_eaten + " portions");
    }
}
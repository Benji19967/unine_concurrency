package synchronization;

public class SavagesBasic {
    static int N = 10; // pot capacity
    static int pot_counter = N;
    static int num_savages_left = Integer.MAX_VALUE; // don't want to init this to 0 in case the cook runs before the
                                                     // savages.
    static int num_savages_have_eaten = 0;
    static boolean is_refilling = false;
    static ReadWriteLock lock = new ReadWriteLock();

    static class Savage implements Runnable {
        int id;

        public Savage(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            // Keep track of how many savages left to know when cook can return
            synchronized (lock) {
                if (num_savages_left == Integer.MAX_VALUE) { // first thread
                    num_savages_left = 1;
                } else {
                    num_savages_left++;
                }
            }

            // Try to eat. If pot is empty, ask cook to refill.
            while (true) {
                synchronized (lock) {
                    if (!is_refilling && pot_counter > 0) {
                        pot_counter--;
                        num_savages_left--;
                        num_savages_have_eaten++;
                        return;
                    } else if (pot_counter == 0 && !is_refilling)
                        is_refilling = true;
                }
            }
        }
    }

    static class Cook implements Runnable {
        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    // Check if all savages have eaten
                    if (num_savages_left == 0)
                        return;
                    if (is_refilling) {
                        pot_counter = N;
                        is_refilling = false;
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        int num_savages = (args.length >= 1 ? Integer.parseInt(args[0]) : 100);
        int num_threads = num_savages + 1; // cook

        System.out.println("Start with " + num_savages + " savages and 1 cook");
        // Create threads
        Thread[] threads = new Thread[num_threads];
        for (int i = 0; i < num_savages; i++) {
            threads[i] = new Thread(new Savage(i));
        }
        threads[num_threads - 1] = new Thread(new Cook());

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
        System.out.println(num_savages_have_eaten + " savages have eaten ");
    }
}

package synchronization;

import static java.lang.Math.sqrt;

public class Primes {

    static int counter;
    final static Object lock = new Object();

    static int primeCounter;
    final static Object lockPrimes = new Object();

    static class PrimeThread implements Runnable {
        int id;
        int start;
        int end;
        int count;

        public PrimeThread(int id, int start, int end, int count) {
            this.id = id;
            this.start = start;
            this.end = end;
            this.count = count;
        }

       // Ex 1.1
       @Override
       public void run() {
           for (int i = start; i < end; i++) {
               if (isPrime(i)) {
                   synchronized (lockPrimes) {
                       primeCounter++;
                   }
               }
           }
       }

        // // Ex 1.2
        // @Override
        // public void run() {
        //     int num;
        //     while (counter < count) {
        //         synchronized (lock) {
        //             num = counter;
        //             counter++;
        //         }
        //         if (isPrime(num)) {
        //             synchronized (lockPrimes) {
        //                 primeCounter++;
        //             }
        //         }
        //     }
        // }

        public boolean isPrime(int num) {
            if (num <= 2) return true;
            else if (num % 2 == 0) return false;

            // brute force -- Todo: use sieve
            for (int i = 3; i <= sqrt(num); i = i + 2) {
                if (num % i == 0) return false;
            }
            return true;
        }
    }

    public static void main(String[] args) {
        int t = Integer.parseInt(args[0]);
        int n = Integer.parseInt(args[1]);

        System.out.println("t: " + t);
        System.out.println("n: " + n);

        counter = 0;
        primeCounter = 0;
        int intervalSize = n / t;
        int start = 0;
        Thread[] threads = new Thread[t];
        for (int i = 0; i < t; i++) {
            threads[i] = new Thread(new PrimeThread(i, start, start + intervalSize, n));
            start += intervalSize;
        }
        long time = System.currentTimeMillis();
        // Start threads
        for (int i = 0; i < t; i++) {
            threads[i].start();
        }
        // Wait for threads completion
        for (int i = 0; i < t; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.println(t + ": found " + primeCounter + " primes " + " in " + time + " ms");
    }
}

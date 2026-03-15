package synchronization;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.sqrt;

// TODO try:
// volatile variables instead of locks

public class Primes {

    static AtomicInteger counter = new AtomicInteger(0);
    final static Object lock = new Object();

    static AtomicInteger primeCounter = new AtomicInteger(0);
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

//       // Ex 1.1
//       @Override
//       public void run() {
//           for (int i = start; i < end; i++) {
//               if (isPrime(i)) {
//                   primeCounter.getAndIncrement();
//               }
//           }
//       }

         // Ex 1.2
         @Override
         public void run() {
             int num;
             while ((num = counter.getAndIncrement()) < count) {
                 if (isPrime(num)) {
                     primeCounter.getAndIncrement();
                 }
             }
         }

        public boolean isPrime(int num) {
            if (num < 2) return false;
            else if (num == 2) return true;
            else if (num % 2 == 0) return false;

            for (int i = 3; i <= sqrt(num); i = i + 2) {
                if (num % i == 0) return false;
            }
            return true;
        }
    }

    public static int countPrimes(int num) {
        boolean[] is_prime = new boolean[num+1];
        Arrays.fill(is_prime, true);
        is_prime[0] = false;
        is_prime[1] = false;

        int p = 2;
        while (p < num) {
            for (int i = 2*p; i <= num; i += p) {
                is_prime[i] = false;
            }
            p++;
            while(!is_prime[p] && p < num)
                p++;
        }

        int prime_count = 0;
        for (boolean b : is_prime) if (b) prime_count++;
        return prime_count;
    }

    public static void main(String[] args) {
        int t = Integer.parseInt(args[0]);
        int n = Integer.parseInt(args[1]);

        System.out.println("t: " + t);
        System.out.println("n: " + n);

//        long time = System.currentTimeMillis();
//        primeCounter = countPrimes(n);

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

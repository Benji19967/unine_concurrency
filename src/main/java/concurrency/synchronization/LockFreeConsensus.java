package synchronization;

import java.util.concurrent.atomic.AtomicReference;

import synchronization.LockFreeConsensus.CompareAndSetConsensus;
import synchronization.LockFreeConsensus.ConsensusThread;

public class LockFreeConsensus {
    static AtomicReference<Integer> decision = new AtomicReference<Integer>();

    static class CompareAndSetConsensus implements Consensus {
        public int decide(int v) {
            boolean success = decision.compareAndSet(null, v);
            return decision.get();
        }
    }

    static class ConsensusThread implements Runnable {
        int id;
        CompareAndSetConsensus consensus;

        public ConsensusThread(int id, CompareAndSetConsensus consensus) {
            this.id = id;
            this.consensus = consensus;
        }

        @Override
        public void run() {
            consensus.decide(id);
        }
    }

    public static void main(String[] args) {
        int T = (args.length >= 1 ? Integer.parseInt(args[0]) : 10);
        CompareAndSetConsensus consensus = new CompareAndSetConsensus();

        Thread[] threads = new Thread[T];

        for (int i = 0; i < T; i++) {
            threads[i] = new Thread(new ConsensusThread(i, consensus));
        }
        long time = System.currentTimeMillis();
        // Start threads
        for (int i = 0; i < T; i++) {
            threads[i].start();
        }

        // Wait for threads completion
        for (int i = 0; i < T; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println(T + ": finished in " + time + " ms, with consensus: " + decision.get());
    }

}

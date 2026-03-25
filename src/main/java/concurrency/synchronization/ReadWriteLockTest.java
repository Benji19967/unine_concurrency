package synchronization;


public class ReadWriteLockTest {

    static ReadWriteLock lock = new ReadWriteLock();
    static long counter = 0;
    static long MAX_COUNT = 20;

    static class Reader implements Runnable {
        int id;

        public Reader(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (counter < MAX_COUNT) {
                lock.lockRead();
                System.out.println("r." + id + ": " + counter);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                lock.unlockRead();
            }
        }

    }

    static class Writer implements Runnable {
        int id;

        public Writer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (counter < MAX_COUNT) {
                lock.lockWrite();
                if (counter < MAX_COUNT) {
                    System.out.println("w." + id + ": " + counter + " -> " + ++counter);
                }
                lock.unlockWrite();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void main(String[] args) {
        int numReaders = 10;
        int numWriters = 1;

        Thread[] readers = new Thread[numReaders];
        Thread[] writers = new Thread[numWriters];

        for (int i = 0; i < numReaders; i++) {
            readers[i] = new Thread(new Reader(i));
        }
        for (int i = 0; i < numWriters; i++) {
            writers[i] = new Thread(new Writer(i));
        }

		long time = System.currentTimeMillis();
		// Start threads
        for (int i = 0; i < numReaders; i++) {
            readers[i].start();
        }
        for (int i = 0; i < numWriters; i++) {
            writers[i].start();
        }
		// Wait for threads completion
        for (int i = 0; i < numReaders; i++) {
			try {
				readers[i].join();
			} catch (InterruptedException e) {
			}
        }
        for (int i = 0; i < numWriters; i++) {
			try {
				writers[i].join();
			} catch (InterruptedException e) {
			}
        }
		time = System.currentTimeMillis() - time;
		System.out.println("Finished with total of " + counter + " in " + time + " ms");
    }
}
package synchronization;

public class ReadWriteLock {
    int numReaders = 0;
    boolean writer = false;

    public synchronized void lockRead() {
        while (writer) {
            try {
                // Note: spurious wakeups can occur,
                // (wakeup eventhough notify() and notifyAll() were not called)
                // (allowed by JVM) so we use a while loop to recheck the condition
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                System.err.println("Thread Interrupted");
            }
        } 
        // System.out.println("ACQUIRE READ: " + Thread.currentThread().getName());
        numReaders++;
    }

    public synchronized void unlockRead() {
        numReaders--;
        if (numReaders == 0) {
            notifyAll();
        }
    }

    public synchronized void lockWrite() {
        while (numReaders > 0 || writer) {
            try {
                // Note: spurious wakeups can occur,
                // (wakeup eventhough notify() and notifyAll() were not called)
                // (allowed by JVM) so we use a while loop to recheck the condition
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                System.err.println("Thread Interrupted");
            }
        } 
        // System.out.println("ACQUIRE WRITE: " + Thread.currentThread().getName());
        writer = true;
    }

    public synchronized void unlockWrite() {
        writer = false;
        notifyAll();
    }
}
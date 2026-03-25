package synchronization;

import java.util.LinkedList;

// Note: methods are not synchronized, we synchronize on the request object already.
// Otherwise, we get a deadlock on r.wait() since it wouldn't release the object lock!

// Guarantees threads are processes in FIFO order
// Only one thread can write at one time, but many can read at the same time

public class ReadWriteLockFIFO {

    public class Request {
        boolean isWriter;
        boolean granted = false;

        public Request(boolean isWriter) {
            this.isWriter = isWriter;
        }
    }

    private final Object lock = new Object();
    int numReaders = 0;
    boolean isWriterActive = false;
    LinkedList<Request> q = new LinkedList<>();

    public void lockRead() {
        Request r = new Request(false);
        synchronized(lock) {
            q.add(r);
            tryGrantNext();
        }

        synchronized(r) {
            while (!r.granted) {
                try {
                    // Note: spurious wakeups can occur,
                    // (wakeup eventhough notify() and notifyAll() were not called)
                    // (allowed by JVM) so we use a while loop to recheck the condition
                    r.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                    System.err.println("Thread Interrupted");
                }
            }
        }
    }

    public void unlockRead() {
        synchronized(lock) {
            numReaders--;
            tryGrantNext();
        }
    }


    public void lockWrite() {
        Request r = new Request(true);
        synchronized(lock) {
            q.add(r);
            tryGrantNext();
        }

        synchronized(r) {
            while (!r.granted) {
                try {
                    // Note: spurious wakeups can occur,
                    // (wakeup eventhough notify() and notifyAll() were not called)
                    // (allowed by JVM) so we use a while loop to recheck the condition
                    r.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                    System.err.println("Thread Interrupted");
                }
            }
        }
    }

    public void unlockWrite() {
        synchronized(lock) {
            // Note: order matters here, otherwise deadlock if 'writer = true' and the next
            // two lines are in reversed order
            isWriterActive = false;
            tryGrantNext();
        }
    }

    private void tryGrantNext() {
        // No thread is active
        if (!isWriterActive && numReaders == 0) {
            if (q.isEmpty()) return;

            Request r = q.peekFirst();

            if (r.isWriter) {
                q.removeFirst();
                isWriterActive = true;

                synchronized(r) { // Synchronize on r to avoid missed notify signal
                    r.granted = true;
                    r.notify();
                }
            } else {
                // grant all consecutive readers
                while (!q.isEmpty() && !q.peekFirst().isWriter) {
                    Request reader = q.removeFirst();  // safe remove
                    numReaders++;

                    synchronized (reader) {
                        reader.granted = true;
                        reader.notify();
                    }
                }
            }
        }
    }
}
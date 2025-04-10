package pcd.concurrent.shared.sync;

import pcd.concurrent.shared.utils.Utils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrierImpl {
    private final int parties;
    private int count;
    private final Runnable barrierCommand;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition trip = lock.newCondition();

    public CyclicBarrierImpl(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException("parties must be positive");
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

    public CyclicBarrierImpl(int parties) {
        this(parties, null);
    }

    public int getParties() {
        return parties;
    }

    public void reset() {
        lock.lock();
        try {
            count = parties; // Reset the barrier
        } finally {
            lock.unlock();
        }
    }

    public int await() throws InterruptedException {
        lock.lock();
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            int index = --count; // Decrease count for the current thread

            Utils.log(""+index,Thread.currentThread().getName());
            if (index == 0) {  // Last thread to arrive
                if (barrierCommand != null) {
                    barrierCommand.run();  // Execute the barrier command
                }
                reset();  // Reset the barrier for the next use
                trip.signalAll();  // Notify all threads to proceed
                return 0;
            }

            // Wait until the last thread arrives
            while (count > 0) {
                Utils.log("here"+count,Thread.currentThread().getName());
                trip.await();
            }

            return index;  // Return the thread's index
        } finally {
            lock.unlock();
        }
    }
}

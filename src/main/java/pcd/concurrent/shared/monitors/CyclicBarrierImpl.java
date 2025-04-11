package pcd.concurrent.shared.monitors;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrierImpl {
    private final int parties;
    private int count;
    private int generation = 0;
    private boolean broken = false;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition trip = lock.newCondition();
    private final Runnable barrierAction;

    public CyclicBarrierImpl(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException("Parties must be greater than 0.");
        this.parties = parties;
        this.count = parties;
        this.barrierAction = barrierAction;
    }

    public CyclicBarrierImpl(int parties) {
        this(parties, null);
    }

    public int await() throws InterruptedException, BrokenBarrierException {
        lock.lock();
        try {
            if (broken) throw new BrokenBarrierException();

            int arrivalGeneration = generation;

            if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            }

            count--;
            if (count == 0) {
                try {
                    if (barrierAction != null) {
                        barrierAction.run();
                    }
                    nextGeneration();
                    return 0;
                } catch (Throwable t) {
                    breakBarrier();
                    throw new BrokenBarrierException();
                }
            }

            while (arrivalGeneration == generation && !broken) {
                try {
                    trip.await();
                } catch (InterruptedException ie) {
                    if (!broken) breakBarrier();
                    throw ie;
                }
            }

            if (broken) throw new BrokenBarrierException();
            return parties - count - 1;
        } finally {
            lock.unlock();
        }
    }

    private void nextGeneration() {
        trip.signalAll();
        count = parties;
        generation++;
    }

    private void breakBarrier() {
        broken = true;
        trip.signalAll();
    }

    public void reset() {
        lock.lock();
        try {
            breakBarrier();
            broken = false;
            count = parties;
            generation++;
        } finally {
            lock.unlock();
        }
    }

    public boolean isBroken() {
        lock.lock();
        try {
            return broken;
        } finally {
            lock.unlock();
        }
    }
}


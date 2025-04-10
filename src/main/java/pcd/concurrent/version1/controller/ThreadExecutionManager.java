package pcd.concurrent.version1.controller;

import pcd.concurrent.shared.ExecutionManager;
import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.monitors.StopFlag;
import pcd.concurrent.shared.utils.Utils;
import pcd.concurrent.version1.workers.BoidSublistWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
//import pcd.concurrent.shared.sync.CyclicBarrier;

import java.util.concurrent.CyclicBarrier;
import java.util.function.Consumer;

public class ThreadExecutionManager implements ExecutionManager {

    private final static int nWorkers = Runtime.getRuntime().availableProcessors() + 1;

    private final List<Thread> workerThreads;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;

    private final BoidsModel model;

    public ThreadExecutionManager(BoidsModel model) {
        this.model = model;
        this.workerThreads = new ArrayList<>();
        this.velocityBarrier = new CyclicBarrier(nWorkers + 1); //+1 for main simulation loop thread
        this.positionBarrier = new CyclicBarrier(nWorkers + 1, model::updateSpatialGrid);
        this.pauseFlag = new PauseFlag();
        this.stopFlag = new StopFlag();
    }

    public List<Thread> getWorkerThreads() {
        return workerThreads;
    }

    private void doForEachThread(Consumer<Thread> action) {
        for (Thread worker : workerThreads) {
            action.accept(worker);
        }
    }

    void resetSynchronizers(){
        stopFlag.reset();
        pauseFlag.reset();
        velocityBarrier.reset();
        positionBarrier.reset();
    }

    @Override
    public void startWorkers() {
        int boidCount = model.getBoidNumber();
        int boidsPerThread = (int) Math.ceil((double) boidCount / nWorkers);

        for (int i = 0; i < boidCount; i += boidsPerThread) {
            String workerName = "Worker-" + (i + 1);
            List<Boid> boidSubset = model.getBoidsSubset(i, Math.min(i + boidsPerThread, boidCount));

            BoidSublistWorker updater = new BoidSublistWorker(
                    workerName,
                    model,
                    boidSubset,
                    velocityBarrier,
                    positionBarrier,
                    pauseFlag,
                    stopFlag
            );
            Thread worker = new Thread(updater);
            worker.start();
            for (Boid boid : boidSubset) { boid.setCurrentHandler(worker.getName()); }
            this.workerThreads.add(worker);
        }
    }

    @Override
    public void stopWorkers() {
        stopFlag.set();

        doForEachThread(Thread::interrupt);

        doForEachThread((thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        workerThreads.clear();
    }

    @Override
    public void pauseWorkers() {
        pauseFlag.set();
    }

    @Override
    public void resumeWorkers() {
        synchronized (pauseFlag) {
            pauseFlag.reset();
        }
    }

    @Override
    public void awaitStepCompletion() {
        try {
            velocityBarrier.await();
            positionBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        }
    }
}

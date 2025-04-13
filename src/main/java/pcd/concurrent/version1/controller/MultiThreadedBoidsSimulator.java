package pcd.concurrent.version1.controller;

import pcd.concurrent.shared.controller.InputListener;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.utils.Utils;
import pcd.concurrent.shared.view.BoidsView;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;

public class MultiThreadedBoidsSimulator implements InputListener {

    private static final int FRAMERATE = 60;

    private final BoidsModel model;
    private final int steps;
    private Optional<BoidsView> view;
    private int framerate;
    private Thread mainLoopThread;

    private final ThreadExecutionManager threadExecution;

    public MultiThreadedBoidsSimulator(BoidsModel model, int steps) {
        this.model = model;
        this.steps = steps;
        this.view = Optional.empty();

        this.threadExecution = new ThreadExecutionManager(model);
    }

    public MultiThreadedBoidsSimulator(BoidsModel model) {
        this(model, -1);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void startSimulation() {
        model.newBoids();
        threadExecution.resetSynchronizers();
        threadExecution.startWorkers();
        notifyWorkersCreated();

        mainLoopThread = new Thread(() -> {
            try {
                if(this.steps > 0) {
                    runSimulationWithTimings(steps);
                }else {
                    runSimulation();
                }

            } catch (InterruptedException | BrokenBarrierException e) {
                Utils.log("Error in main simulation loop thread", Thread.currentThread().getName());
            }
        });
        mainLoopThread.start();
    }

    private void notifyWorkersCreated() {
        view.ifPresent(boidsView ->
                boidsView.onWorkersCreated(threadExecution.getWorkerThreads().stream().map(Thread::getName).toList())
        );
    }

    private void stopSimulation() {
        threadExecution.stopWorkers();

        interruptMainLoop();
    }

    private void interruptMainLoop() {
        if (mainLoopThread != null && mainLoopThread.isAlive()) {
            mainLoopThread.interrupt();
            try {
                mainLoopThread.join();
            } catch (InterruptedException e) {
                Utils.log("Main simulation loop thread interrupted.", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
            }
        }
    }

    private void pauseSimulation(){
        threadExecution.pauseWorkers();
    }

    private void resumeSimulation(){
        threadExecution.resumeWorkers();
    }

    public void runSimulation() throws InterruptedException, BrokenBarrierException {
        while (!Thread.currentThread().isInterrupted()) {
            var t0 = System.currentTimeMillis();

            threadExecution.awaitStepCompletion();
            if (Thread.currentThread().isInterrupted()) break;

            updateView(t0);
        }
    }

    public void runSimulationWithTimings(int steps){
        long start = System.nanoTime();
        for (int i = 0; i < steps; i++) {
            var t0 = System.currentTimeMillis();

            threadExecution.awaitStepCompletion();
            if (Thread.currentThread().isInterrupted()) break;

            updateView(t0);
        }
        long end = System.nanoTime();
        double durationMs = (end - start) / 1_000_000.0;
        System.out.printf("Completed %d steps in %.2f ms (%.2f ms/step)%n",
                steps, durationMs, durationMs / steps);
    }

    private void updateView(long t0) {
        if (view.isPresent()) {
            view.get().update(framerate);
            var t1 = System.currentTimeMillis();
            var dtElapsed = t1 - t0;
            var frameratePeriod = 1000 / FRAMERATE;

            if (dtElapsed < frameratePeriod) {
                try {
                    Thread.sleep(frameratePeriod - dtElapsed);
                } catch (Exception ignored) {
                }
                framerate = FRAMERATE;
            } else {
                framerate = (int) (1000 / dtElapsed);
            }
        }
    }

    @Override
    public synchronized void started() {
        startSimulation();
    }

    @Override
    public synchronized void paused() {
        pauseSimulation();
    }

    @Override
    public void resumed() {
        resumeSimulation();
    }

    @Override
    public synchronized void stopped() {
        stopSimulation();
    }

    @Override
    public synchronized void boidsNumberChanged(int nBoids) {
        model.setBoidNumber(nBoids);
    }

    @Override
    public synchronized void separationWeightChanged(int sWeight) {
        model.setSeparationWeight(0.1 * sWeight);
    }

    @Override
    public synchronized void alignmentWeightChanged(int hWeight) {
        model.setAlignmentWeight(0.1 * hWeight);
    }

    @Override
    public synchronized void cohesionWeightChanged(int cWeight) {
        model.setCohesionWeight(0.1 * cWeight);
    }
}
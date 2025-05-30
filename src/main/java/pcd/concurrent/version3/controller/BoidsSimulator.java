package pcd.concurrent.version3.controller;

import pcd.concurrent.shared.controller.InputListener;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.utils.Utils;
import pcd.concurrent.shared.view.BoidsView;

import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;

public class BoidsSimulator implements InputListener {

    private static final int FRAMERATE = 60;

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private int framerate;

    private final VThreadExecutionManager virtualThreadExecutor;
    private Thread mainLoopThread;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        this.view = Optional.empty();

        this.virtualThreadExecutor = new VThreadExecutionManager(model);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    private void startSimulation() {
        model.newBoids();
        virtualThreadExecutor.resetSynchronizers();

        mainLoopThread = new Thread(this::runSimulation);
        mainLoopThread.start();
    }

    private void stopSimulation() {
        virtualThreadExecutor.stopWorkers();
        interruptMainLoop();
    }

    private void interruptMainLoop() {
        if (mainLoopThread != null && mainLoopThread.isAlive()) {
            mainLoopThread.interrupt();
            try {
                mainLoopThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void pauseSimulation(){
        virtualThreadExecutor.pauseWorkers();
    }

    private void resumeSimulation(){
        virtualThreadExecutor.resumeWorkers();
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

    public void runSimulation() {
        while (!Thread.currentThread().isInterrupted()) {
            var t0 = System.currentTimeMillis();

            virtualThreadExecutor.awaitStepCompletion();

            if (Thread.currentThread().isInterrupted()) break;

            updateView(t0);
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
package pcd.concurrent.version2.controller;

import pcd.concurrent.shared.controller.InputListener;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.utils.Utils;
import pcd.concurrent.shared.view.BoidsView;

import java.util.Optional;

public class TaskBoidsSimulator implements InputListener {

    private static final int FRAMERATE = 60;

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private int framerate;

    private final TaskExecutionManager taskExecutor;
    private Thread mainLoopThread;
    private final int steps;

    public TaskBoidsSimulator(BoidsModel model, int steps) {
        this.model = model;
        this.steps = steps;
        this.view = Optional.empty();

        this.taskExecutor = new TaskExecutionManager(model);
    }

    public TaskBoidsSimulator(BoidsModel model) {
        this(model, 0);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    private void startSimulation() {
        model.newBoids();
        taskExecutor.resetSynchronizers();

        mainLoopThread = new Thread(() -> {
            try {
                if(this.steps > 0) {
                    runSimulationWithTimings(steps);
                }else {
                    runSimulation();
                }
            } catch (InterruptedException e) {
                Utils.log("Main simulation loop thread interrupted.", Thread.currentThread().getName());
                taskExecutor.shutdownExecutor();
            }
        });
        mainLoopThread.start();
    }

    private void runSimulationWithTimings(int steps) {
        long start = System.nanoTime();
        for (int i = 0; i < steps; i++) {
            var t0 = System.currentTimeMillis();

            taskExecutor.awaitStepCompletion();
            if (Thread.currentThread().isInterrupted()) break;

            updateView(t0);
        }
        long end = System.nanoTime();
        double durationMs = (end - start) / 1_000_000.0;
        System.out.printf("Completed %d steps in %.2f ms (%.2f ms/step)%n",
                steps, durationMs, durationMs / steps);

    }

    private void stopSimulation() {
        taskExecutor.stopWorkers();
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
        taskExecutor.pauseWorkers();
    }

    private void resumeSimulation(){
        taskExecutor.resumeWorkers();
    }

    public void runSimulation() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            var t0 = System.currentTimeMillis();

            taskExecutor.awaitStepCompletion();

            if(Thread.currentThread().isInterrupted()) break;

            updateView(t0);
        }
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
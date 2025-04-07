package pcd.version2.controller;

import pcd.common.ExecutionManager;
import pcd.version1.controller.ThreadExecutionManager;
import pcd.version1.utils.Utils;
import pcd.version2.model.Boid;
import pcd.version2.model.BoidsModel;
import pcd.version2.monitors.PauseFlag;
import pcd.version2.monitors.StopFlag;
import pcd.version2.tasks.PositionUpdateTask;
import pcd.version2.tasks.VelocityUpdateTask;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TaskExecutionManager implements ExecutionManager {

    private final static int nWorkers = Runtime.getRuntime().availableProcessors() + 1;

    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    private final BoidsModel model;
    private final ExecutorService executor;
    private final List<Callable<Void>> tasks;

    public TaskExecutionManager(BoidsModel model) {
        this.model = model;
        this.executor = Executors.newFixedThreadPool(nWorkers);
        this.tasks = new ArrayList<>();
        this.pauseFlag = new PauseFlag();
        this.stopFlag = new StopFlag();
    }

    void resetSynchronizers() {
        stopFlag.reset();
        pauseFlag.reset();
    }

    private <T extends Callable<Void>> void executeTasks(Class<T> taskClass) {
        int boidCount = model.getBoidNumber();
        int chunkSize = (int) Math.ceil((double) boidCount / nWorkers);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < boidCount; i += chunkSize) {
            try {
                List<Boid> boidsSubset = model.getBoidsSubset(i, Math.min(i + chunkSize, boidCount));
                tasks.add(taskClass.getConstructor(
                                List.class,
                                BoidsModel.class,
                                PauseFlag.class,
                                StopFlag.class
                        ).newInstance(
                                    boidsSubset,
                                    model,
                                    pauseFlag,
                                    stopFlag
                        ));
            } catch (Exception e) {
                Utils.log("Error while creating tasks: " + e.getMessage(), Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                shutdownExecutor();
            }
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void startWorkers() {

    }

    public void shutdownExecutor() {
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void stopWorkers() {
        stopFlag.set();
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
        executeTasks(VelocityUpdateTask.class);
        executeTasks(PositionUpdateTask.class);
        model.updateSpatialGrid();
    }
}

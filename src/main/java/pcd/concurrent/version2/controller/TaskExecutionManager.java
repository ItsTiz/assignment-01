package pcd.concurrent.version2.controller;

import pcd.concurrent.shared.ExecutionManager;
import pcd.concurrent.shared.utils.Utils;
import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.monitors.StopFlag;
import pcd.concurrent.version2.tasks.PositionUpdateTask;
import pcd.concurrent.version2.tasks.VelocityUpdateTask;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TaskExecutionManager implements ExecutionManager {

    private final static int nWorkers = Runtime.getRuntime().availableProcessors() + 1;

    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    private final BoidsModel model;
    private ExecutorService executor;

    public TaskExecutionManager(BoidsModel model) {
        this.model = model;
        this.executor = Executors.newFixedThreadPool(nWorkers);
        this.pauseFlag = new PauseFlag();
        this.stopFlag = new StopFlag();
    }

    void resetSynchronizers() {
        stopFlag.reset();
        pauseFlag.reset();
    }

    private void mountAndRunTasks() {
        executeTasks(createTasks(VelocityUpdateTask.class));
        executeTasks(createTasks(PositionUpdateTask.class));
    }

    private <T extends Callable<Void>> List<Callable<Void>> createTasks(Class<T> taskClass) {
        int boidCount = model.getBoidNumber();
        int boidsPerTask = (int) Math.ceil((double) boidCount / nWorkers);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < boidCount; i += boidsPerTask) {
            try {
                List<Boid> boidsSubset = model.getBoidsSubset(i, Math.min(i + boidsPerTask, boidCount));
                Constructor<T> taskConstructor =
                        taskClass.getConstructor(List.class, BoidsModel.class, PauseFlag.class, StopFlag.class);
                T task = taskConstructor.newInstance(boidsSubset, model, pauseFlag, stopFlag);
                tasks.add(task);
            } catch (Exception e) {
                Utils.log("Error while creating tasks: " + e.getMessage(), Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                shutdownExecutor();
            }
        }

        return tasks;
    }

    private void executeTasks(List<Callable<Void>> tasks) {
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException | RejectedExecutionException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void startWorkers() {}

    public void shutdownExecutor() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
            Utils.log("Executor has been shutdown.",Thread.currentThread().getName());
        }
    }

    public void resetExecutor() {
        this.executor = Executors.newFixedThreadPool(nWorkers);
        Utils.log("Executor has been reset.",Thread.currentThread().getName());
    }

    @Override
    public void stopWorkers() {
        stopFlag.set();
        shutdownExecutor();
        resetExecutor();
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
        if (stopFlag.isSet() || Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }
        mountAndRunTasks();
        model.updateSpatialGrid();
    }
}

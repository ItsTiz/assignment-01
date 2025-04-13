package pcd.concurrent.version3.controller;

import pcd.concurrent.shared.ExecutionManager;
import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.monitors.StopFlag;
import pcd.concurrent.shared.utils.Utils;
import pcd.concurrent.version3.tasks.PositionUpdateTask;
import pcd.concurrent.version3.tasks.VelocityUpdateTask;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VThreadExecutionManager implements ExecutionManager {

    private final BoidsModel model;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;
    private final List<Thread> velocityThreads;
    private final List<Thread> positionThreads;

    public VThreadExecutionManager(BoidsModel model){
        this.model = model;
        this.pauseFlag = new PauseFlag();
        this.stopFlag = new StopFlag();
        this.velocityThreads = new ArrayList<>();
        this.positionThreads = new ArrayList<>();
    }

    void resetSynchronizers(){
        stopFlag.reset();
        pauseFlag.reset();
    }

    @Override
    public void startWorkers() {

    }

    @Override
    public void stopWorkers() {
        stopFlag.set();
        velocityThreads.clear();
        positionThreads.clear();
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
        if(stopFlag.isSet()) Thread.currentThread().interrupt();
        velocityThreads.addAll(createTasks(VelocityUpdateTask.class));
        waitAll(velocityThreads);

        positionThreads.addAll(createTasks(PositionUpdateTask.class));
        waitAll(positionThreads);

        velocityThreads.clear();
        positionThreads.clear();

        //model.updateSpatialGrid();
    }

    private void waitAll(List<Thread> threads) {
        for (Thread t : List.copyOf(threads)) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Utils.log("Task interrupted abruptly.", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
            }
        }
    }

    private <T extends Runnable> List<Thread> createTasks(Class<T> taskClass) {

        List<Thread> threads = new ArrayList<>();
        for (Boid boid: model.getBoids()) {
            try {
                Constructor<T> taskConstructor = taskClass.getConstructor(
                        Boid.class,
                        BoidsModel.class,
                        PauseFlag.class,
                        StopFlag.class);
                T task = taskConstructor.newInstance(boid, model, pauseFlag, stopFlag);
                Thread vThread = Thread.ofVirtual().start(task);
                threads.add(vThread);
            } catch (Exception e) {
                Utils.log("Error while creating tasks: " + e.getMessage(), Thread.currentThread().getName());
                Thread.currentThread().interrupt();
            }
        }

        return threads;
    }

}

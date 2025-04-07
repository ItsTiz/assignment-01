package pcd.common;

import java.util.concurrent.BrokenBarrierException;

public interface ExecutionManager {

    void startWorkers();

    void stopWorkers();

    void pauseWorkers();

    void resumeWorkers();

    void awaitStepCompletion();

}

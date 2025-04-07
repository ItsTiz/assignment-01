package pcd.concurrent.shared;

public interface ExecutionManager {

    void startWorkers();

    void stopWorkers();

    void pauseWorkers();

    void resumeWorkers();

    void awaitStepCompletion();

}

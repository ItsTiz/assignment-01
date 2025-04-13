package pcd.concurrent.shared;

/**
 * Interface for managing the lifecycle and coordination of worker threads
 * in a concurrent simulation. It provides methods to start, stop, pause,
 * and resume workers, as well as to synchronize on the completion of a
 * simulation step.
 *
 * Implementations of this interface are responsible for ensuring proper
 * coordination and synchronization among threads according to the
 * specific requirements of the concurrent system.
 *
 * @author Tiziano Vuksan
 * @email tiziano.vuksan@studio.unibo.it
 */
public interface ExecutionManager {

    /**
     * Starts the worker threads and initializes any necessary structures
     * for execution.
     */
    void startWorkers();

    /**
     * Signals the worker threads to stop execution and performs cleanup
     * if necessary.
     */
    void stopWorkers();

    /**
     * Pauses the execution of worker threads. The threads should wait
     * without progressing the simulation until resumed.
     */
    void pauseWorkers();

    /**
     * Resumes the execution of paused worker threads.
     */
    void resumeWorkers();

    /**
     * Blocks the calling thread until all worker threads have completed
     * their current simulation step.
     */
    void awaitStepCompletion();

}

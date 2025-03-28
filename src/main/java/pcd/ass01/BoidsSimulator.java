package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class BoidsSimulator {

    private static final int FRAMERATE = 25;

    private BoidsModel model;
    private Optional<BoidsView> view;
    private List<Thread> workerThreads;

    public void setUpdaterWorkers(List<UpdaterWorker> updaterWorkers) {
        this.updaterWorkers = updaterWorkers;
    }

    private List<UpdaterWorker> updaterWorkers;
    private int framerate;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        workerThreads = new ArrayList<>();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void setWorkerThreads(List<Thread> workerThreads) {
        this.workerThreads = workerThreads;
    }

    public void updateUpdaterWorkers(CountDownLatch doneSignal) {

        int nWorkers =  workerThreads.size();
        int jobSize = BoidsSimulation.N_BOIDS / nWorkers;
        int from = 0;
        int to = jobSize - 1;

        for (UpdaterWorker updater: updaterWorkers) {

            if (updater == updaterWorkers.get(updaterWorkers.size() - 1)) {
                to = BoidsSimulation.N_BOIDS - 1;
            }

            updater.setDoneSignal(doneSignal);
            updater.setBoidsSubset(model.getBoidsSubset(from, to));

            from = to + 1;
            to += jobSize;
        }
    }

    private List<Thread> divideWorkLoad(int nWorkers, CountDownLatch doneSignal) {
        int jobSize = BoidsSimulation.N_BOIDS / nWorkers;
        int from = 0;
        int to = jobSize - 1;

        final List<Thread> workers = new ArrayList<>();
        final List<UpdaterWorker> updaters = new ArrayList<>();

        for (int i = 0; i < nWorkers; i++) {
            if (i == nWorkers - 1) {
                to = BoidsSimulation.N_BOIDS - 1;
            }

            UpdaterWorker updater = new UpdaterWorker(
                    "Worker-" + (i + 1),
                    doneSignal,
                    model.getBoidsSubset(from, to),
                    model
            );

            Thread worker = new Thread(updater);

            worker.start();
            workers.add(worker);
            updaters.add(updater);
            from = to + 1;
            to += jobSize;
        }

        setUpdaterWorkers(updaters);

        return workers;
    }

    public void runSimulation() throws InterruptedException {

        int nWorkers = Runtime.getRuntime().availableProcessors() + 1;
        CountDownLatch doneSignal = new CountDownLatch(nWorkers);
        Utils.log("" + nWorkers, Thread.currentThread().getName());
        setWorkerThreads(divideWorkLoad(nWorkers, doneSignal));

        while (true) {
            var t0 = System.currentTimeMillis();
            var boids = model.getBoids();

            /*
             * Improved correctness: first update velocities...
             */

            doneSignal = new CountDownLatch(nWorkers);

            updateUpdaterWorkers(doneSignal);

            doneSignal.await();

            /*
             * ..then update positions
             */
            for (Boid boid : boids) {
                boid.updatePos(model);
            }


            if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var frameratePeriod = 1000 / FRAMERATE;

                if (dtElapsed < frameratePeriod) {
                    try {
                        Thread.sleep(frameratePeriod - dtElapsed);
                    } catch (Exception ex) {
                    }
                    framerate = FRAMERATE;
                } else {
                    framerate = (int) (1000 / dtElapsed);
                }
            }
        }
    }
}

package pcd.sequential;

import java.util.Optional;

public class BoidsSimulator {

    private BoidsModel model;
    private final int steps;
    private Optional<BoidsView> view;
    
    private static final int FRAMERATE = 60;
    private int framerate;
    
    public BoidsSimulator(BoidsModel model) {
        this(model, 0);
    }

    public BoidsSimulator(BoidsModel model, int steps) {
        this.model = model;
        this.steps = steps;
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    private void runIndefinitely(){
        while (true) {
            var t0 = System.currentTimeMillis();
            var boids = model.getBoids();
    		/*
    		for (Boid boid : boids) {
                boid.update(model);
            }
            */

            /*
             * Improved correctness: first update velocities...
             */
            for (Boid boid : boids) {
                boid.updateVelocity(model);
            }

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
                var framratePeriod = 1000/FRAMERATE;

                if (dtElapsed < framratePeriod) {
                    try {
                        Thread.sleep(framratePeriod - dtElapsed);
                    } catch (Exception ex) {}
                    framerate = FRAMERATE;
                } else {
                    framerate = (int) (1000/dtElapsed);
                }
            }

        }


    }

    public void runSimulation(){
        if(steps > 0)
            runSimulationWithTimings(steps);
        else
            runIndefinitely();

    }

    public void runSimulationWithTimings(int steps) {
        long start = System.nanoTime();
        for (int i = 0; i < steps; i++) {
            var t0 = System.currentTimeMillis();
    		var boids = model.getBoids();
    		/*
    		for (Boid boid : boids) {
                boid.update(model);
            }
            */
    		
    		/* 
    		 * Improved correctness: first update velocities...
    		 */
    		for (Boid boid : boids) {
                boid.updateVelocity(model);
            }

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
                var framratePeriod = 1000/FRAMERATE;
                
                if (dtElapsed < framratePeriod) {		
                	try {
                		Thread.sleep(framratePeriod - dtElapsed);
                	} catch (Exception ex) {}
                	framerate = FRAMERATE;
                } else {
                	framerate = (int) (1000/dtElapsed);
                }
    		}
            System.out.println(i+"/"+steps);
    	}
        long end = System.nanoTime();
        double durationMs = (end - start) / 1_000_000.0;
        System.out.printf("Completed %d steps in %.2f ms (%.2f ms/step)%n",
                steps, durationMs, durationMs / steps);
    }
}

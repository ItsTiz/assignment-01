package pcd.test;

import pcd.concurrent.version1.controller.MultiThreadedBoidsSimulator;
import pcd.concurrent.version2.controller.TaskBoidsSimulator;
import pcd.concurrent.version3.controller.VThreadBoidsSimulator;
import pcd.concurrent.version3.controller.VThreadExecutionManager;
import pcd.sequential.BoidsSimulator;

import java.util.concurrent.BrokenBarrierException;

public class BoidsBenchmark {
    final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1920;
    final static int ENVIRONMENT_HEIGHT = 1080;
    static final double MAX_SPEED = 4;
    static final double PERCEPTION_RADIUS = 30.0;
    static final double AVOID_RADIUS = 20.0;

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
        int boidCount = 8000;
        int steps = 500;

        var model = new pcd.sequential.BoidsModel(
                boidCount,
                SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED,
                PERCEPTION_RADIUS,
                AVOID_RADIUS);

        var cmodel = new pcd.concurrent.shared.model.BoidsModel( boidCount,
                SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED,
                PERCEPTION_RADIUS,
                AVOID_RADIUS);

//        System.out.println("Sequential version:");
//
//        BoidsSimulator sim = new BoidsSimulator(model, steps);
//        sim.runSimulation();

//        System.out.println("Multithreaded version:");
//
//        MultiThreadedBoidsSimulator multisim = new MultiThreadedBoidsSimulator(cmodel, steps);
//        multisim.started();
//
//        System.out.println("Task based version:");
//        TaskBoidsSimulator tasksim = new TaskBoidsSimulator(cmodel, steps);
//        tasksim.started();

        System.out.println("Virtual threads version:");
        VThreadBoidsSimulator vthreadsim = new VThreadBoidsSimulator(cmodel, steps);
        vthreadsim.started();
    }
}

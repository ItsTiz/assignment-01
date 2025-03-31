package pcd.version1.controller;

import pcd.version1.model.BoidsModel;
import pcd.version1.view.BoidsView;

import java.util.concurrent.BrokenBarrierException;

public class BoidsSimulation {

    final static int N_BOIDS = 3000;

    final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1920;
    final static int ENVIRONMENT_HEIGHT = 1080;
    static final double MAX_SPEED = 4;
    static final double PERCEPTION_RADIUS = 30.0;
    static final double AVOID_RADIUS = 20.0;

    final static int SCREEN_WIDTH = 1920;
    final static int SCREEN_HEIGHT = 1080;


    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        var model = new BoidsModel(
                N_BOIDS,
                SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED,
                PERCEPTION_RADIUS,
                AVOID_RADIUS);
        var sim = new BoidsSimulator(model);
        var view = new BoidsView(model, SCREEN_WIDTH, SCREEN_HEIGHT);
        sim.attachView(view);
        sim.startSimulation();
    }
}

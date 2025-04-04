package pcd.version1.view;

import pcd.version1.model.Boid;
import pcd.version1.model.BoidsModel;
import pcd.version1.utils.Utils;

import javax.swing.*;
import java.awt.*;

public class BoidsPanel extends JPanel {

	private BoidsView view; 
	private BoidsModel model;
    private int framerate;

    public BoidsPanel(BoidsView view, BoidsModel model) {
    	this.model = model;
    	this.view = view;
    }

    public void setFrameRate(int framerate) {
    	this.framerate = framerate;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        
        var w = view.getWidth();
        var h = view.getHeight();
        var envWidth = model.getWidth();
        var xScale = w/envWidth;

        var boids = model.getBoids();

        for (Boid boid : boids) {
            String hexColor = boid.getColorHex() != null ? boid.getColorHex() : "#000000";
            g.setColor(Color.decode(hexColor));
            var x = boid.getPos().x();
        	var y = boid.getPos().y();
        	int px = (int)(w/2 + x*xScale);
        	int py = (int)(h/2 - y*xScale);
            g.fillOval(px,py, 8, 8);
        }
        
        g.setColor(Color.BLACK);
        g.drawString("Num. Boids: " + boids.size(), 10, 25);
        g.drawString("Framerate: " + framerate, 10, 40);
   }
}

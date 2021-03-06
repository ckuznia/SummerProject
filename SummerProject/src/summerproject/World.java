package summerproject;


import java.io.Serializable;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.state.StateBasedGame;

public class World implements Entity, Serializable {
	
	/*
	 * TODO:
	 * 
	 * Don't render stuff off screen, use max and min x and y values to see if it gets on screen (use Shape max and min values)
	 * Only check for collisions with on/screen items
	 * Every time an Entity is created (like Turret for example) it reloads the image, this only needs to be done once. If I can
	 * make every image static and only loaded once that would save a lot of computation time (It would be neat to be able to load
	 * them in the Load state)
	 * 
	 * Later:
	 * name world files based off of date, time, and world version (serialVersionUID)
	 * Don't ever get graphics contexts while in-game, this is resource intensive
	 * and should all be gathered before the game starts
	 */
	
	/**
	 * Version of the class, need to update this number if the class
	 * becomes incompatible with the earlier version. Serialization
	 * is used for saving the state of the game.
	 */
	private static final long serialVersionUID = 3L;
	
	private static final Logger log = LogManager.getLogger(World.class);
	
	private Image background; // Don't need an actual image to draw on since we are using slick2d, everything will just be its own separate image drawn in the container
	
	public static ArrayList<Shape> entities = new ArrayList<Shape>();
	
	private Player player;
	private ArrayList<IronOre> ironOre = new ArrayList<IronOre>();
	
	public World(final int WIDTH, final int HEIGHT) {
		try {
			player = new Player(WIDTH, HEIGHT);
			entities.add(player);
			
			// Initializing the image
			background = new Image(WIDTH, HEIGHT);
			Graphics g2 = background.getGraphics();
			g2.setBackground(new Color(235, 225, 159));
			g2.clear();
		} catch (SlickException e) {
			log.error("Failed to create main World image.", e);
		}
		
		generateIronOre(WIDTH, HEIGHT);
	}
	
	private void generateIronOre(int WIDTH, int HEIGHT) {
		for(int oreGenerated = 0; oreGenerated < 500;) {
			int oreX = (int)(Math.random() * WIDTH * 2);
			int oreY = (int)(Math.random() * HEIGHT * 2);
			oreX *= ((int)(Math.random() * 2) == 0 ? 1 : -1);
			oreY *= ((int)(Math.random() * 2) == 0 ? 1 : -1);
			double chanceToSpawn = 0;
			
			if(ironOre.size() == 0 ) {
				// Placing the first iron ore
				IronOre ore = new IronOre(oreX, oreY);
				ironOre.add(ore);
				oreGenerated++;
				continue;
			}
			else {
				for(int n = 0; n < ironOre.size(); n++) {
					// Calculating square distance
					double distance = Math.pow(ironOre.get(n).getCenterX() - oreX, 2) + Math.pow(ironOre.get(n).getCenterY() - oreY, 2);
					// The chance to spawn increases when closer to another ore (within a specified distance)
					int clusterAmount = 100000; // The higher the cluster, the tighter the groups of ore
					if(distance <= 2000) chanceToSpawn += clusterAmount / distance;
				}
			}
		
			// Spawn if == or < chance to spawn
			if((int)(Math.random() * 100) <= chanceToSpawn) {
				IronOre iron = new IronOre(oreX, oreY);
				ironOre.add(iron);
				oreGenerated++;
			}
			// Otherwise, do not spawn the ore nothing
		}
		entities.addAll(ironOre);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) {
		// Updating all entities
		for(Shape entity: entities) ((Entity) entity).update(container, game, delta);
		
		for(Shape entity: entities) {
			if(entity != player) {
				entity.setX(entity.getX() - player.getOffsetX());
				entity.setY(entity.getY() - player.getOffsetY());
			}
		}
		player.clearOffset();
		
		checkCollisions();
	}
	
	private void checkCollisions() {
		
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) {
		g.drawImage(background, 0, 0);
		
		for(Shape entity: entities) ((Entity) entity).render(container, game, g);
	}
	
	public static void rotateImageTo(Image image, float centerX, float centerY, float centerX2, float centerY2) {
		double height = centerY2 - centerY;
		double width = centerX2 - centerX;
		// The images starting position is pointing north, therefore 90 degrees is added to account for that
		float angle = ((float) Math.toDegrees(Math.atan(height / width))) + 90;
		if(centerX > centerX2) angle -= 180;
		image.setRotation(angle);
	}
	
	public static void rotateTo(Image image, float x, float y, Shape shape) {
		rotateImageTo(image, x, y, shape.getX(), shape.getY());
	}
	
	public static long getVersion() {
		return serialVersionUID;
	}

	@Override
	public void handleCollision(Entity entity) {}
}

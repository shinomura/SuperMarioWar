package smw.entity;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.ArrayList;

import smw.Drawable;
import smw.Game;
import smw.Updatable;
import smw.gfx.Palette.ColorScheme;
import smw.gfx.Sprite;
import smw.settings.Debug;
import smw.ui.PlayerControlBase;
import smw.ui.screen.GameFrame;

public class Player extends Rectangle implements Drawable, Updatable{
	/**
	 * Autogenerated!
	 */
	private static final long serialVersionUID = -4197702383138211374L;
	private static final long RESPAWN_WAIT_MS = 2000;
	static final int WRAP_AROUND_FACTOR = (GameFrame.res_width - Sprite.IMAGE_WIDTH);
	private Sprite  sprite;
	public PlayerPhysics physics;
	private Score score;
	private final int playerIndex;
	private boolean crushed = false;
	private boolean killed = false;
	private long respawnTime;
	
	/** Indicates whether a player is falling through a tile by pressing down key. */
	public boolean isFallingThrough = false;
	/** The starting height of falling through a "solid on top" tile. */
	public int fallHeight = 0;
	public boolean pushedDown = false;
	
	public Player(PlayerControlBase playerControl, int playerIndex){	
		physics = new PlayerPhysics(playerControl, this);
		sprite  = new Sprite();
		score   = new Score();
		this.playerIndex = playerIndex;
	}
	
	void setBounds(int newX, int newY){
	  //TODO might be a faster way (width/height never change)
		setBounds(newX, newY, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT);			
	}
	
	public Image getImage(){
		return sprite.getImage();
	}
	
	public void init(int newX, int newY){
	  init(newX, newY, getRandomSprite());
	}
	
	public void init(int newX, int newY, String image){
		setBounds(newX, newY);
		
		//TODO this is obviously not staying in
		int i = (int)(4*Math.random());
		ColorScheme color = ColorScheme.YELLOW;
		if(i == 0){
		  color = ColorScheme.RED;
		}
		else if(i == 1){
      color = ColorScheme.GREEN;
    }
		else if(i == 2){
      color = ColorScheme.BLUE;
    }
		
		sprite.init(image, color);
	}
	
	/*** This method is to get the state ready to move ***/
	public void prepareToMove(){
		physics.update();
	}
		
	public void move(Player[] players){	
		if(crushed){
			//TODO this is messy and should be a method called at the beginning
			if(!score.isOut() && respawnTime < System.currentTimeMillis()){
				crushed = false;
				setBounds(300, 100);
				sprite.setJumping();
			}
			
			return; //Do not want to do anything with someone who is crushed
		}
			
		float dx = physics.getVelocityX();
		float dy = physics.getVelocityY();
		
		sprite.update(dx, dy, physics.isJumping, physics.isSkidding);
		int newX, newY = 0;
		
		newX = (int) (x + dx);
		newY = (int) (y + dy);
		    
    if(newX < 0){
      newX += GameFrame.res_width;
    }
		
    newX = Game.world.getCollisionX(this, newX);
    newY = Game.world.getCollisionY(this, newX, newY);
    
    newX = newX % GameFrame.res_width;
    newY = newY % GameFrame.res_height;
    
    //#############################################################
    // Player collision
    //#############################################################
		boolean xCollide = false;
		//This is definitely not right... but its kinda cool that it sort of works
		for(Player p : players){
			if(p.playerIndex != playerIndex){
				if(!p.crushed && p.intersects(newX, y, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT)){
				  if(p.x > newX){
				    newX = p.x - Sprite.IMAGE_WIDTH - 1;
				  }
				  else{
				    newX = p.x + Sprite.IMAGE_HEIGHT + 1;
				  }
				  
					xCollide = true;
					break;
				}
			}
		}
		
		for(Player p : players){
			if(p.playerIndex != playerIndex){
				if(!p.crushed && p.intersects(x, newY, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT)){
					if(!xCollide){
						if(p.getY() < newY){
						  p.score.increaseScore();
							crush();
						}
						else{
						  score.increaseScore();
							p.crush();
						}
					}
					physics.collideWithFloor();
					newY = y;
					break;
				}
			}
		}

		setBounds(newX, newY);					
	}
		
	protected void crush(){
		crushed = true;
		score.decreaseScore();
		//TODO mk didn't like this but if you want to play with it, make gameFrame static and this works 
		//Game.gameFrame.bump();
		respawnTime = System.currentTimeMillis() + RESPAWN_WAIT_MS;
		sprite.crush();
	}
	
	public void superDeath() {
		killed = true;
		score.decreaseScore();
	}
	
	public void poll(){
		physics.poll();
	}
	
	public void draw(Graphics2D graphics, ImageObserver observer){
	  graphics.drawImage(sprite.getImage(), x, y, observer);
	  if(x > WRAP_AROUND_FACTOR) {
	    graphics.drawImage(sprite.getImage(), x-GameFrame.res_width + 1, y, observer);
	  }
	}
	
	public int getScore(){
		return score.getScore();
	}
	
	public static String getRandomSprite(){
	    ArrayList<String> result = new ArrayList<String>();
	    File folder = new File(Player.class.getClassLoader().getResource("sprites/").getFile());
	    File[] listOfFiles = folder.listFiles();
	    for(File f : listOfFiles){
	      result.add(f.getName());
	    }
	    
	    return result.get((int)(Math.random()*result.size()));	  
	}
	
	public void land3ed() {
	  sprite.clearAction();
	}

  public boolean isOut() {
    return score.isOut();
  }

  @Override
  public void update(float timeDif_ms) {
    prepareToMove();//TODO this should probably include moving but didnt want to mess with player collision
  }
}

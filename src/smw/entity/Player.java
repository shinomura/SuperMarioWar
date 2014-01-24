package smw.entity;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import smw.Game;
import smw.gfx.Palette.ColorScheme;
import smw.gfx.Sprite;
import smw.level.Level;
import smw.level.Tile;
import smw.ui.PlayerControlBase;

public class Player extends Rectangle{
	/**
	 * Autogenerated!
	 */
	private static final long serialVersionUID = -4197702383138211374L;
	private static final long RESPAWN_WAIT_MS = 2000;
	Sprite  sprite;
	PlayerPhysics physics;
	final int playerIndex;
	boolean crushed = false;
	long respawnTime;
	
	public Player(PlayerControlBase playerControl, int playerIndex){	
		physics = new PlayerPhysics(playerControl);
		sprite  = new Sprite();
		this.playerIndex = playerIndex;
	}
	
	void setBounds(int newX, int newY){
	  //TODO might be a faster way (width/height never change)
		setBounds(newX, newY, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT);			
	}
	
	public Image getImage(){
		return sprite.getImage();
	}
	
	public void init(int newX, int newY, String image){
		setBounds(newX, newY);
		sprite.init(image, ColorScheme.GREEN);
	}
	
	/*** This method is to get the state ready to move ***/
	public void prepareToMove(){
		//physics.update();
	}
	
	public void move(Player[] players){	
		if(crushed){
			//TODO this is messy and should be a method called at the beginning
			if(respawnTime < System.currentTimeMillis()){
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
		
		// TODO - RPG - this is awful, but at least we can detect the floor sort of	
		// Need to fix add detection for X axis
		// Probably need to check each tile around the players new point like Mitch did with "intersects"
		// physics will need to be updated to NOT update if we can't move in a certain x or y direction
		if (Game.level.getTileTypeAtPx(newX + Level.TILE_SIZE + 2, newY) == Tile.SOLID) {
      if (this.intersects(newX, newY, Level.TILE_SIZE, Level.TILE_SIZE)) {
        newX = x;
        System.out.println("X1");
        physics.collideWithWall();
      }
    } 
		
		if (Game.level.getTileTypeAtPx(newX - 3, newY) == Tile.SOLID) {
      if (this.intersects(newX, newY, Level.TILE_SIZE, Level.TILE_SIZE)) {
        newX = x;
        System.out.println("X2");
        physics.collideWithWall();
      }
    }
		
		if (Game.level.getTileTypeAtPx(newX, newY + Level.TILE_SIZE) == Tile.SOLID) {
		  if (this.intersects(x, newY, Level.TILE_SIZE, Level.TILE_SIZE)) {
  		  newY = y;
  		  physics.collideWithFloor();
		  }
		}
		
		if (Game.level.getTileTypeAtPx(newX + Level.TILE_SIZE, newY + Level.TILE_SIZE) == 1) {
      if (this.intersects(x, newY, Level.TILE_SIZE, Level.TILE_SIZE)) {
        newY = y;
        physics.collideWithFloor();
      }
    }

		physics.update();
		
		boolean xCollide = false;
		//This is definitely not right... but its kinda cool that it sort of works
		for(Player p : players){
			if(p.playerIndex != playerIndex){
				if(!p.crushed && p.intersects(newX, y, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT)){
					newX = x;
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
							crush();
						}
						else{
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
		respawnTime = System.currentTimeMillis() + RESPAWN_WAIT_MS;
		sprite.crush();
	}
	
	public void poll(){
		physics.poll();
	}
	
	public void draw(Graphics2D graphics, ImageObserver observer){
	  graphics.drawImage(sprite.getImage(), x, y, observer);
	}
	
	public int getScore(){
		return 1001; //TODO
	}
}
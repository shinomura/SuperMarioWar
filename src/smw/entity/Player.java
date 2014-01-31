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
	Score score;
	final int playerIndex;
	boolean crushed = false;
	long respawnTime;
	
	public Player(PlayerControlBase playerControl, int playerIndex){	
		physics = new PlayerPhysics(playerControl);
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
		
		if (x != newX) {
      if (x > newX) {
        //Moving left
        if(Game.level.getTileTypeAtPx(newX, y) == Tile.SOLID){
          newX = newX + (newX % Level.TILE_SIZE) + 1;
          physics.collideWithWall();
        }
      }
      else{
        //Moving right
        if(Game.level.getTileTypeAtPx(newX + Sprite.IMAGE_WIDTH, y) == Tile.SOLID){
          newX = newX - (newX % Level.TILE_SIZE) - 1;
          physics.collideWithWall();
        }
      }
    } 

    if (y != newY){
      if (y < newY) {
        //Moving down. We want to check every block that is under the sprite. This is from the first 
        //             Pixel (newX) to the last (newX + (Sprite.Width - 1))
        int tile1 = Game.level.getTileTypeAtPx(newX, newY + Sprite.IMAGE_HEIGHT);
        int tile2 = Game.level.getTileTypeAtPx(newX + Sprite.IMAGE_WIDTH - 1, newY + Sprite.IMAGE_HEIGHT);
        
        if(tile1 != Tile.NONSOLID || tile2 != Tile.NONSOLID){
          //TODO this might need some work once others are introduced, but making sure 
          //     it isn't a situation where the player is pressing down to sink through
          if((tile1 == Tile.SOLID_ON_TOP || tile1 == Tile.NONSOLID) &&
             (tile2 == Tile.SOLID_ON_TOP || tile2 == Tile.NONSOLID) &&
             (physics.playerControl.isDown())) {//either pushing down or already did and working through the block
            
          }
          else{ 
            newY = newY - (newY % Level.TILE_SIZE); //Just above the floor
            physics.collideWithFloor();
          }
        }
      }
      else {
        //Moving up
        if(Game.level.getTileTypeAtPx(newX, newY) == Tile.SOLID ||
           Game.level.getTileTypeAtPx(newX + Sprite.IMAGE_WIDTH - 1, newY) == Tile.SOLID){
          //For now, this is ok because up velocity isn't fast enough to get a pixel up,
          //but probably want to make sure 
          physics.collideWithCeiling();
        }
      }
    }

		//physics.update();
		
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

	/*****************************************************
	 *TODO this is not complete but good enough for now
	 *
	 *THIS WILL WRAP THE SCREEN AROUND CHA CHING mk
	 ******************************************************/
		newX = newX % 640;
		newY = newY % 480;
		
		if(newX < 0){
		  newX += 640;
		}
		
		if(newY < 0){
		  newY += 480;
		}
		
		setBounds(newX, newY);					
	}
		
	protected void crush(){
		crushed = true;
		//TODO mk didn't like this but if you want to play with it, make gameFrame static and this works 
		//Game.gameFrame.bump();
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
		return score.getScore();
	}
}

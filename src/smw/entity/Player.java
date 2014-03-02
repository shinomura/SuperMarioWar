package smw.entity;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.ArrayList;

import smw.Drawable;
import smw.Game;
import smw.Updatable;
import smw.gfx.AboveArrow;
import smw.gfx.Palette.ColorScheme;
import smw.gfx.Font;
import smw.gfx.SpawnAnimation;
import smw.gfx.Sprite;
import smw.settings.Debug;
import smw.ui.PlayerControlBase;
import smw.ui.screen.GameFrame;
import smw.world.Structures.Warp.Direction;
import smw.world.Structures.WarpExit;

public class Player extends Rectangle2D.Float implements Drawable, Updatable{
	/** Autogenerated! */
	private static final long serialVersionUID = -4197702383138211374L;
	
	private static final long RESPAWN_WAIT_MS = 2000;
	/** Overall count down to player death from being idle. */
	private static final int IDLE_COUNTDOWN_MS = 6000;
	/** Count down to display number sprite on player from being idle. */
	private static final int IDLE_DISPLAY_TIME_MS = 3000;
	/** Offset for count down number sprite on player. */
	private static final int NUMBER_SPRITE_OFFSET = 8;
	static final int WRAP_AROUND_FACTOR = (GameFrame.res_width - Sprite.IMAGE_WIDTH);
	private Sprite  sprite;
	public PlayerPhysics physics;
	private Score score;
	private final int playerIndex;
	private int idleCountdown_ms = IDLE_COUNTDOWN_MS;
	private SpawnAnimation spawnAnimation;
	///////////////////////////////////////////////////
	// WARPING
	///////////////////////////////////////////////////
	private WarpExit warpExit;
	private boolean warpingIn = false;
	private boolean warpingOut = false;
	private int warpFactorX = 0;
	private int warpFactorY = 0;
	private float warpingAnimationDistance;
	private static final float WARP_VELOCITY = 1.0f;
	private static final int MAX_WARPING_ANIMATION_DISTANCE = 32;
	
	private boolean crushed = false;
	private boolean killed = false;
	private long respawnTime;
	public ColorScheme color;
  private boolean canFall = true;
  
	public Player(PlayerControlBase playerControl, int playerIndex){	
		physics = new PlayerPhysics(playerControl);
		sprite  = new Sprite();
		score   = new Score();
		
		width = Sprite.IMAGE_WIDTH;
		height = Sprite.IMAGE_HEIGHT;

		this.width  = Sprite.IMAGE_WIDTH;
		this.height = Sprite.IMAGE_HEIGHT;
		this.playerIndex = playerIndex;
	}
	
	public Image getImage(){
		return sprite.getImage();
	}
	
	public void init(){
	  init(getRandomSprite());
	}
	

	public void init(String image){
	  Game.world.setSpawnPoint(this);
	  
		//TODO this is obviously not staying in
		int i = (int)(4*Math.random());
		color = ColorScheme.YELLOW;
		
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
		spawnAnimation = new SpawnAnimation((int)x, (int)y, color);
    //TODO mk not sure how i feel about this
		AboveArrow arrow = new AboveArrow(this);
		Game.world.drawablesLayer3.add(arrow);
		Game.world.updatables.add(arrow);
    Game.world.updatables.add(spawnAnimation);
    Game.world.drawablesLayer2.add(spawnAnimation);
	}
	
	private void initForSpawn(){
    crushed = false;
	  killed = false;
    Game.world.setSpawnPoint(this);
    spawnAnimation = new SpawnAnimation((int)x, (int)y, color);
    //TODO mk not sure how i feel about this
    Game.world.updatables.add(spawnAnimation);
    Game.world.drawablesLayer2.add(spawnAnimation);
    sprite.setJumping();
	}
	
	/*** This method is to get the state ready to move ***/
	public void prepareToMove(float timeDif){
		physics.update(timeDif);
	}
		
	public void move(Player[] players){	
	  if(spawnAnimation != null && !spawnAnimation.shouldBeRemoved()){
	    return;
	  }
	  else{
	    spawnAnimation = null;
	  }
	  
		if(crushed){
			//TODO this is messy and should be a method called at the beginning
			if(!score.isOut() && respawnTime < System.currentTimeMillis()){
			  initForSpawn();
			}
			
			return; //Do not want to do anything with someone who is crushed
		}
		
		if(killed){		    
		  y += physics.getVelocityY();
		  
		  //TODO this is messy and should be a method called at the beginning
      if(!score.isOut() && respawnTime < System.currentTimeMillis()){
        initForSpawn();
      }
      
      //TODO should probably do some sort of max y else if the player is out and it keeps growing,
      //     there might be some sort of issue
      return;
		}
    
    if(warpingOut){

      warpingAnimationDistance += WARP_VELOCITY;
      
      x += warpFactorX*WARP_VELOCITY;
      y += warpFactorY*WARP_VELOCITY;  
      
      if(warpingAnimationDistance >= MAX_WARPING_ANIMATION_DISTANCE){
        warpingAnimationDistance = 0;
        warpingOut = false;
      }
      
      return;
    }
    
		if(warpingIn){

		  warpingAnimationDistance += WARP_VELOCITY;

		  x += warpFactorX*WARP_VELOCITY;
      y += warpFactorY*WARP_VELOCITY;
      
		  if(warpingAnimationDistance >= MAX_WARPING_ANIMATION_DISTANCE){
		    warpingAnimationDistance = 0;
		    
		    x = warpExit.x;
		    y = warpExit.y;
		    setWarpFactor(warpExit.direction);
		    
		    warpingIn = false;
		    warpingOut = true;
		  }
		  
		  return;
		}
			
		float dx = physics.getVelocityX();
		float dy = physics.getVelocityY();
		
		sprite.update(dx, dy, physics.isJumping, physics.isSkidding);
		
		float newX = x + dx;
		float newY = y + dy;
		    
    if(newX < 0){
      newX += GameFrame.res_width;
    }
    
    Game.world.testWarps(this, newX, newY);
		
    newX = Game.world.getCollisionX(this, newX);
    newX = newX % GameFrame.res_width;
    
    newY = Game.world.getCollisionY(this, newX, newY);
    newY = newY % GameFrame.res_height;
    
    //#############################################################
    // Player collision
    //#############################################################
		boolean xCollide = false;
		//This is definitely not right... but its kinda cool that it sort of works
		for(Player p : players){
			if(p.playerIndex != playerIndex){
				if(p.isAlive()){
				  //Need some special logic to deal with players who are on the edge. This can probably be simplified
				  if(p.intersects(newX, y, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT)){
				    if(p.x > newX && (p.x - newX < 2*Sprite.IMAGE_WIDTH)){
	            newX = p.x - Sprite.IMAGE_WIDTH - 1;
	          }
	          else{
	            newX = p.x + Sprite.IMAGE_HEIGHT + 1;
	          }
	          
	          xCollide = true;
	          break;
				  }
				  else if((newX + Sprite.IMAGE_WIDTH > GameFrame.res_width) && p.intersects(newX - GameFrame.res_width, y, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT)){
				    newX = p.x - Sprite.IMAGE_HEIGHT ;
            
            xCollide = true;
            break;
				  }
				  else if((p.x  + Sprite.IMAGE_WIDTH > GameFrame.res_width) && p.intersects(newX + GameFrame.res_width, y, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT)){
				    newX = p.x + Sprite.IMAGE_WIDTH;

            xCollide = true;
            break;
				  }
				}
			}
		}
		
		for(Player p : players){
			if(p.playerIndex != playerIndex){
				if(p.isAlive() && p.intersects(x, newY, Sprite.IMAGE_WIDTH, Sprite.IMAGE_HEIGHT)){
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

		x = newX;
		
		if(y < newY){
		  canFall = false;
		}
		
		canFall |= !physics.playerControl.isDown();
				
		y = newY;
	}
	
	boolean isAlive(){
	  return !crushed && !killed && (spawnAnimation == null || spawnAnimation.shouldBeRemoved());
	}
		
	protected void crush(){
		crushed = true;
		score.decreaseScore();
		Game.bump();
		respawnTime = System.currentTimeMillis() + RESPAWN_WAIT_MS;
		sprite.crush();
		Game.soundPlayer.sfxMip();
	}
	
	public void superDeath() {
    if(!killed){
      killed = true;
      sprite.death();
      physics.death();
      respawnTime = System.currentTimeMillis() + RESPAWN_WAIT_MS;
      score.decreaseScore();
      Game.soundPlayer.sfxDeath();
    }
	}
	
	public void death() {
	  if(!killed){
  	  killed = true;
  	  sprite.death();
  	  physics.death();
  	  respawnTime = System.currentTimeMillis() + RESPAWN_WAIT_MS;
  	  Game.soundPlayer.sfxDeath();
  	  score.decreaseScore();
	  }
	}
	
	public void poll(){
		physics.poll();
	}
	
	boolean drawToBack(){
	  return warpingIn || warpingOut;
	}
	
	 public boolean drawToBack(Graphics2D graphics, ImageObserver observer){
	   if(drawToBack()){
       graphics.drawImage(sprite.getImage(), (int)x, (int)y, observer);
       if(x > WRAP_AROUND_FACTOR) {
         graphics.drawImage(sprite.getImage(), (int)(x-GameFrame.res_width + 1), (int)y, observer);
       }
       
       return true;
	   }
	   
	   return false;
	 }
	
	public void draw(Graphics2D graphics, ImageObserver observer){
	  if(drawToBack() || (spawnAnimation != null && !spawnAnimation.shouldBeRemoved())){
	    return;
	  }
	  
	  graphics.drawImage(sprite.getImage(), (int)x, (int)y, observer);
	  
	  // Handle player idle time.
	  Font font = Font.getInstance();
	  if (idleCountdown_ms <= IDLE_DISPLAY_TIME_MS) {
	    font.drawBoxedNumber(graphics, idleCountdown_ms / 1000, color.index, (int)x + NUMBER_SPRITE_OFFSET,
	      (int)y + NUMBER_SPRITE_OFFSET, observer);
	  }
	  
	  // Handle x-axis screen wrap.
	  if (x > WRAP_AROUND_FACTOR) {
	    graphics.drawImage(sprite.getImage(), (int)(x-GameFrame.res_width + 1), (int)y, observer);
	    // Wrap around idle count down timer.
	    if (idleCountdown_ms <= IDLE_DISPLAY_TIME_MS) {
	      font.drawBoxedNumber(graphics, idleCountdown_ms / 1000, color.index,
	        (int)(x-GameFrame.res_width + 1) + NUMBER_SPRITE_OFFSET, (int)y + NUMBER_SPRITE_OFFSET, observer);
	    }
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

  public boolean isOut() {
    return score.isOut();
  }

  @Override
  public void update(float timeDif_ms) {    
    prepareToMove(timeDif_ms);//TODO this should probably include moving but didnt want to mess with player collision
    
    if ((spawnAnimation == null || spawnAnimation.shouldBeRemoved()) && !crushed && killed) {
      physics.updateForDeath(timeDif_ms);
    } else {
      if (Debug.PLAYER_DEATH_IDLE) {
        if (!physics.playerControl.isJumping() && physics.playerControl.getDirection() == 0) {
          // Not jumping or moving so apply idle count down. 
          idleCountdown_ms -= timeDif_ms;
          if (idleCountdown_ms <= 0) {
            idleCountdown_ms = IDLE_COUNTDOWN_MS;
            death();
          }
        } else {
          idleCountdown_ms = IDLE_COUNTDOWN_MS;
        }
      }
    }
  }

  public void warp(Direction direction, WarpExit warpExit) {
    warpingIn = true;
    this.warpExit = warpExit;
    Game.soundPlayer.sfxWarp();
    
    setWarpFactor(direction);
  }
  
  void setWarpFactor(Direction direction){
    warpFactorX = 0;
    warpFactorY = 0;
    
    switch(direction){
      case UP:    warpFactorY = -1;
                  break;
      case DOWN:  warpFactorY =  1;
                  break;
      case RIGHT: warpFactorX =  1;
                  break;
      case LEFT:  warpFactorX = -1;
                  break;
    }
  }

  public void slipOnIce() {
    physics.slipOnIce();
  }

  @Override
  public boolean shouldBeRemoved() {
    return isOut();
  }

  /**
   * Returns if this player is dead.
   * @return true = dead
   */
  public boolean isDead() {
	  return this.killed;
  }

  public boolean canFall() {
    return canFall;
  }
}

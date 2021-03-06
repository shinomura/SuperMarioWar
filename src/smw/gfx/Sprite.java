package smw.gfx;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import smw.gfx.Palette.ColorScheme;

public class Sprite {
	public static enum Action {
		NONE(0), RUNNING_STEP(1), RUNNING_NO_STEP(0), JUMPING(2), SKIDDING(3), DYING(4), CRUSHED(5);

	  private final int index;
	  private Action(int index) {
	    this.index = index;
	  }
	};

	public static enum Direction {
		RIGHT(0), LEFT(1);
		
		public static int NUM_DIRECTIONS = 2;
	  public final int index;
	  private Direction(int index) {
	    this.index = index;
	  }
	};

  public static final int IMAGE_WIDTH = 32;
  public static final int IMAGE_HEIGHT = 32;
  public static final int NUM_IMAGES = 6;
  public static final long TIME_TO_HALF_STEP_MS = 60;
  
  //Keep a copy of each direction to avoid image flipping processing
  BufferedImage[][] sprites = new BufferedImage[Direction.NUM_DIRECTIONS][NUM_IMAGES];
  
  Action    currentAction    = Action.NONE;
  Direction currentDirection  = Direction.RIGHT;
 
  long timeActionChange_ms;

  public Sprite() {
    timeActionChange_ms = System.currentTimeMillis();
  }

  public void init(String image, ColorScheme colorScheme){
  	currentAction    = Action.NONE;
    currentDirection  = Direction.RIGHT;
    
    try {
      BufferedImage bigImg = ImageIO.read(this.getClass().getClassLoader().getResource("sprites/" + image));
      // Must convert to a BufferedImage that allows transparency (read above uses TYPE_3BYTE_BGR).
      BufferedImage convertedImg = new BufferedImage(bigImg.getWidth(), bigImg.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
      convertedImg.getGraphics().drawImage(bigImg, 0, 0, null);

      // Get the right color and make the magenta alpha 0.
      Palette p = Palette.getInstance();
      p.loadPalette();
      p.colorSprite(colorScheme, convertedImg);
      
      // Create Transform to flip image for left facing versions.
      AffineTransform result = AffineTransform.getScaleInstance(-1.0, 1.0);
      result.translate(-IMAGE_WIDTH, 0);
      AffineTransformOp op = new AffineTransformOp(result, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
      
      for (int i = 0; i < NUM_IMAGES; i++) {
        sprites[Direction.RIGHT.index][i] = convertedImg.getSubimage(i * IMAGE_WIDTH, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        sprites[Direction.LEFT.index ][i] = op.filter(sprites[Direction.RIGHT.index][i], null);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  // It is assumed this method is only called after collision detection passed
  public void update(float dx, float dy, boolean isJumping, boolean isSkidding) {
    if( dx != 0){
      currentDirection = (dx < 0) ? Direction.LEFT : Direction.RIGHT;
    }
    
    if(isJumping){
      currentAction = Action.JUMPING;
    }
    else if(isSkidding){
      currentAction = Action.SKIDDING;
    }
    else{
      switch (currentAction) {
  	    case NONE:
  	      checkForRunning(dx);
  	      break;
  	    case RUNNING_STEP:
  	      checkForRunningStepChange(dx, Action.RUNNING_NO_STEP);
  	      break;
  	    case RUNNING_NO_STEP:
  	      checkForRunningStepChange(dx, Action.RUNNING_STEP);
  	      break;
  	    case JUMPING:
  	      currentAction = (dx == 0) ? Action.NONE : Action.RUNNING_STEP;
  	    case SKIDDING:
          currentAction = (dx == 0) ? Action.NONE : Action.RUNNING_STEP;
  	    case CRUSHED: // do nothing
  	      break;
  	    case DYING: // do nothing
  	      break;
      }
    }
  }

  private void checkForRunningStepChange(float dx, Action nextRunningAction) {
    if (dx == 0) {
      currentAction = Action.NONE;
    } 
    else {
      long currentTime_ms = System.currentTimeMillis();
      if (currentTime_ms - timeActionChange_ms > TIME_TO_HALF_STEP_MS) {
        currentAction = nextRunningAction;
        timeActionChange_ms = currentTime_ms;
        
      }
    }
  }

  private void checkForRunning(float dx) {
    if (dx != 0) {
      currentAction = Action.RUNNING_STEP;
      timeActionChange_ms = System.currentTimeMillis();
    }
  }

  public BufferedImage getImage() {
    return sprites[currentDirection.index][currentAction.index];
  }

  public void setJumping() {
    currentAction = Action.JUMPING;
  }

	public void movingLeft() {
	  currentDirection = Direction.LEFT;
  }
	
	public void movingRight() {
	  currentDirection = Direction.RIGHT;
  }
	
	public void crush(){
		currentAction = Action.CRUSHED;
	}
	
	public void clearAction() {
	  currentAction = Action.NONE;
	}

  public void death() {
    currentAction = Action.DYING;
  }
}

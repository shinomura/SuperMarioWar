package smw.gfx;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;

import javax.imageio.ImageIO;

import smw.ui.screen.GameFrame;

public class Font {
  private static Font instance;
  private static final int NUM_FONTS = 94;
  private static final int NUM_NUMBERS = 10;
  private static final int NUMBER_HEIGHT = 16;
  private static final int NUMBER_WIDTH = 16;
  private static final int NUM_COLORS = 4;
  
  private final BufferedImage[] largeFont = new BufferedImage[NUM_FONTS];
  private final BufferedImage[] smallFont = new BufferedImage[NUM_FONTS];
  private final BufferedImage[] raceNumbers = new BufferedImage[NUM_NUMBERS];
  private final BufferedImage[][] scoreNumbers = new BufferedImage[ScoreType.NUM_SCORE_TYPES][NUM_NUMBERS];
  private final BufferedImage[][] boxedNumbers = new BufferedImage[NUM_COLORS][NUM_NUMBERS];
  
  enum ScoreType {
    COLOR(0), GRAY(1);
    
    public static final int NUM_SCORE_TYPES = 2;
    public final int index;
    ScoreType(int i) {
      index = i;
    }
  }

  static {
    instance = new Font();
  }

  private Font() { 
  	init();
  }    

  public static Font getInstance() {
      return instance;
  }
  
  void init(){
  	setupFont(largeFont, "fonts/font_large.png");
  	setupFont(smallFont, "fonts/font_small.png");
  	setupRace();
  	setupScore();
  	setupBoxedNumers();
  }

  void setupFont(BufferedImage[] font, String fileName){
		try {		
	    BufferedImage imageBuffer = ImageIO.read(getClass().getClassLoader().getResource(fileName));
	    //The large font file seperates each different character by putting
	    //magenta in the top row where there is a gap between letters
	    int height = imageBuffer.getHeight();
	    int currentPosition = 0;
	    for(int i = 0 ; i < NUM_FONTS ; ++i){
	    	//Find the next character
	    	while(0xffff00ff == imageBuffer.getRGB(++currentPosition, 0));
	    	int start = currentPosition;
	    	//Now find where the character ends
	    	while(0xffff00ff != imageBuffer.getRGB(++currentPosition, 0));
	    	//Store the letter
	    	BufferedImage convertedImg = new BufferedImage(imageBuffer.getWidth(), imageBuffer.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
	      convertedImg.getGraphics().drawImage(imageBuffer, 0, 0, null);
	      
	    	Palette palette = Palette.getInstance();
	    	palette.implementTransparent(convertedImg, 0xffffffff);
	    	font[i] = convertedImg.getSubimage(start, 0, currentPosition - start, height);
	    }
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }   	
	}
	
	void setupRace(){
		try {		
	    BufferedImage imageBuffer = ImageIO.read(getClass().getClassLoader().getResource("fonts/race.png"));
	    BufferedImage convertedImg = new BufferedImage(imageBuffer.getWidth(), imageBuffer.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
      convertedImg.getGraphics().drawImage(imageBuffer, 0, 0, null);

      //Get the right color and make the magenta alpha 0
      Palette p = Palette.getInstance();
      p.implementTransparent(convertedImg);
	    
	    for (int i = 0; i < NUM_NUMBERS; i++) {
	    	raceNumbers[i] = convertedImg.getSubimage(i * NUMBER_WIDTH, 0, NUMBER_WIDTH, NUMBER_HEIGHT);
      }
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }   	
	}
	
	void setupScore(){
		try {		
	    BufferedImage imageBuffer = ImageIO.read(getClass().getClassLoader().getResource("fonts/score.png"));
	    BufferedImage convertedImg = new BufferedImage(imageBuffer.getWidth(), imageBuffer.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
      convertedImg.getGraphics().drawImage(imageBuffer, 0, 0, null);

      //Get the right color and make the magenta alpha 0
      Palette p = Palette.getInstance();
      p.implementTransparent(convertedImg);
	    
	    for (int i = 0; i < NUM_NUMBERS; i++) {
	    	scoreNumbers[ScoreType.COLOR.index][i] = convertedImg.getSubimage(i * NUMBER_WIDTH, 0, NUMBER_WIDTH, NUMBER_HEIGHT);
        scoreNumbers[ScoreType.GRAY.index ][i] = convertedImg.getSubimage(i * NUMBER_WIDTH, NUMBER_HEIGHT, NUMBER_WIDTH, NUMBER_HEIGHT);
      }
    } catch (IOException e) {
	    e.printStackTrace();
    }   	
	}
	
	/** Reads boxed number image, applies transparency, stores sub-image of each number. */
	private void setupBoxedNumers() {
	  final int SIZE_PX = 16;
	  try {
	    BufferedImage imageBuffer = ImageIO.read(getClass().getClassLoader().getResource("gfx/packs/Classic/menu/menu_boxed_numbers.png"));
      BufferedImage convertedImg = new BufferedImage(imageBuffer.getWidth(), imageBuffer.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
      convertedImg.getGraphics().drawImage(imageBuffer, 0, 0, null);
      Palette p = Palette.getInstance();
      p.implementTransparent(convertedImg);
      for (int colorIndex = 0; colorIndex < NUM_COLORS; colorIndex++) {
        for (int numberIndex = 0; numberIndex < NUM_NUMBERS; numberIndex++) {
          boxedNumbers[colorIndex][numberIndex] = convertedImg.getSubimage(numberIndex * SIZE_PX, colorIndex * SIZE_PX, SIZE_PX, SIZE_PX);
        }
      }
	  } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //TODO not sure if this is the most efficient way to do things
  BufferedImage getChar(BufferedImage[] font, char character) {
		//Characters are in order, starting with '!'. 
  	//TODO do some research to make sure this doesn't screw up on other machines
  	int index = (int)(character - '!');
  	
  	if(index >= 0 && index < font.length){
  		return font[index];
  	}
  	
  	return null;
  }
  
  public void drawBoxedNumber(Graphics2D graphics, int number, int color, int x, int y, ImageObserver observer) {
    if ((color >= 0 && color < 4) && (number >= 0 && number < 10)) { 
      graphics.drawImage(boxedNumbers[color][number], x, y, observer);
    }
  }
	
	//TODO decide who should be handling the numbers, but put both kinds in just in case
	public void drawScore(Graphics2D graphics, int score, int x, int y, ImageObserver observer){
		String scoreString = String.format("%04d", score);
		int length = scoreString.length();
		char[] array = scoreString.substring( length - 3, length - 1).toCharArray();
		ScoreType index = (score > 99) ? ScoreType.COLOR : ScoreType.GRAY;

		for(char c : array){
			if(c != '0'){
				index = ScoreType.COLOR;
			}
			graphics.drawImage(getSingleDigitScore((int)(c - '0'), index), x, y, observer);
			x += NUMBER_WIDTH;
		}
		
		graphics.drawImage(getSingleDigitScore(score%10, ScoreType.COLOR), x, y, observer);
	}
	
	public void drawText(Graphics2D graphics, BufferedImage[] font, char[] charArray, int x, int y, ImageObserver observer){
	  if(x < 0){
	    x += GameFrame.res_width;
	  }

		for(char c : charArray){
			BufferedImage i = getChar(font, c);
			if(i != null){
				graphics.drawImage(i, x, y, observer);
				x += i.getWidth();
				if(x > GameFrame.res_width){
				  //Back up and draw the letter wrapped around
				  int newStartingPoint = x - i.getWidth() - GameFrame.res_width;
				  graphics.drawImage(i, newStartingPoint, y, observer);
				  x = newStartingPoint + i.getWidth();
				}
			}
			else{
				//This is a space...should probably just add this to the buffer
				x += 5;
			}
		}
	}
	
	public void drawLargeText(Graphics2D graphics, char[] charArray, int x, int y, ImageObserver observer){
		drawText(graphics, largeFont, charArray, x, y, observer);
	}
	
	public void drawSmallText(Graphics2D graphics, char[] charArray, int x, int y, ImageObserver observer){
		drawText(graphics, smallFont, charArray, x, y, observer);
	}
	
	public Image getSingleDigitScore(int i, ScoreType type){
		if(i < 10){
			return scoreNumbers[type.index][i];
		}
		
		return null;
	}
	
  public BufferedImage getLarge(char character){
  	return getChar(largeFont, character);
  }  
  
  public BufferedImage getSmall(char character){
  	return getChar(smallFont, character);
  }

  //TODO this is AWFUL but only called once at the beginning so not bad for now
  public BufferedImage getLargeText(char[] charArray) {
    int width = 0;
    int height;
        
    for(char c : charArray){
      width += getLarge(c).getWidth();
    }
    
    height = getLarge(charArray[0]).getHeight();
    
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
    width = 0;
    height = 0;
    
    for(char c : charArray){
      BufferedImage charImage = getLarge(c);
      for(int w = 0 ; w < charImage.getWidth() ; ++w){
        for(int h = 0 ; h < charImage.getHeight() ; ++h){
          result.setRGB(w + width, h, charImage.getRGB(w, h));
        }
      }
      
      width  += charImage.getWidth();
    }
    
    return result;
  }
}

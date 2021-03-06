package smw.ui;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/*********************************************************
 * TODO This class is currently for MKs usb snes controller. Didn't
 * have another controller to test/customize with
 *******************************************************/
public class GamePad  extends PlayerControlBase{
		
	/***************************************************
	 * TODO This method is temporary to help store off a controller
	 * until we get settings going
	 * 
	 * @param type - Set to NONE if you want to auto-setup one
	 ****************************************************/
	void setControllerValues(ControllerType type){
		switch(type){
			case SNES_WIN_MK: buttonToComponentMap[PlayerButton.LEFT.index]  =  1;
												buttonToComponentMap[PlayerButton.RIGHT.index] =  1;
												buttonToComponentMap[PlayerButton.DOWN.index]  =  0;
												buttonToComponentMap[PlayerButton.UP.index]    =  0;
												buttonToComponentMap[PlayerButton.JUMP.index]  =  8;
												buttonToComponentMap[PlayerButton.RUN.index]   =  9;
												buttonToComponentMap[PlayerButton.PAUSE.index] = 15;
												pressedValues[PlayerButton.LEFT.index]  = -1.0f;
												pressedValues[PlayerButton.RIGHT.index] =  1.0f;
												pressedValues[PlayerButton.UP.index]    = -1.0f;
												pressedValues[PlayerButton.DOWN.index]  =  1.0f;
												pressedValues[PlayerButton.JUMP.index]  =  1.0f;
												pressedValues[PlayerButton.RUN.index]   =  1.0f;
												pressedValues[PlayerButton.PAUSE.index]  =  1.0f;
												break;
			case LOGITECH_TIM: buttonToComponentMap[PlayerButton.LEFT.index]  = 16;
                         buttonToComponentMap[PlayerButton.RIGHT.index] = 16;
                         //TODO buttonToComponentMap[PlayerButton.DOWN.index] = 2;
                         buttonToComponentMap[PlayerButton.JUMP.index]  = 1;
                         buttonToComponentMap[PlayerButton.RUN.index]   = 0;
                         pressedValues[PlayerButton.LEFT.index]  =  1.0f;
                         pressedValues[PlayerButton.RIGHT.index] =  0.5f;
                         //TODO pressedValues[PlayerButton.DOWN.index] =  0.5f;
                         pressedValues[PlayerButton.JUMP.index]  =  1.0f;
                         pressedValues[PlayerButton.RUN.index]   =  1.0f;
                         //TODO added UP and Pause as well
                         break;
			case SNES_MAC_MK: buttonToComponentMap[PlayerButton.LEFT.index]  =  13;
                        buttonToComponentMap[PlayerButton.RIGHT.index] =  13;
                        buttonToComponentMap[PlayerButton.DOWN.index]  =  14;
                        buttonToComponentMap[PlayerButton.UP.index]    =  14;
                        buttonToComponentMap[PlayerButton.JUMP.index]  =  2;
                        buttonToComponentMap[PlayerButton.RUN.index]   =  3;
                        buttonToComponentMap[PlayerButton.PAUSE.index] =  9;
                        pressedValues[PlayerButton.LEFT.index]  = -1.0f;
                        pressedValues[PlayerButton.RIGHT.index] =  1.0f;
                        pressedValues[PlayerButton.UP.index]    = -1.0f;
                        pressedValues[PlayerButton.DOWN.index]  =  1.0f;
                        pressedValues[PlayerButton.JUMP.index]  =  1.0f;
                        pressedValues[PlayerButton.RUN.index]   =  1.0f;
                        pressedValues[PlayerButton.PAUSE.index] =  1.0f;
                        break;
			case NONE:        
			default:          setup();             
												break;
		}
	}
	
	/*****************************************************
	 * At index GamePadButton.index is the integer value
	 * of the component for the given button
	 *****************************************************/
  int[] buttonToComponentMap = new int[PlayerButton.NUM_BUTTONS_USED];
	
  public final int NUM_BUTTONS;
  Controller controller;
  
  boolean isConnected;
  
  //TODO this seems to work, but might want to reset it 
  //     after every poll in case it moves in memory, no
  //     problems so far though. Really don't need it. Could
  //     just call getComponents everywhere
  final Component[] components;
  
	//TODO verify that touch sensitivity is not an issue
	//    (ie maybe can't use != and need some wiggle room)
  /***********************************************
   * Indexed by component number, this is the 
   * initial state of each component.
   * 
   * Some controllers start as set (1.0f or -1.0f). 
   * Need to document these default values to avoid 
   * false positives.
   ***********************************************/
  final float[] defaultValues;
  /***********************************************
   * Indexed by component number, this array stores
   * the values that a button will be set at when 
   * it is pressed down.
   ***********************************************/
  final float[] pressedValues = new float[PlayerButton.NUM_BUTTONS_USED];
  
  //TODO again, the input is for debugging until settings are stored off
  public GamePad(ControllerType type) {
    setType(type);
    Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

    //Find the controller. TODO this will change but great for now. Grab the first one
    // NOTE: for other controllers, this might be different Type
    for(int i =0;i<ca.length;i++){
      if(ca[i].getType() == Controller.Type.STICK){
      	controller = ca[i];
      	isConnected = true;
      	break;
      }
    }
    
    if (controller == null) {
      throw new IllegalArgumentException(); // TODO - we should probably make our own exception that has a msg
    }
   
    //Need to poll in order to get the default values
    controller.poll();
    components = controller.getComponents();
    NUM_BUTTONS = components.length;
    defaultValues = new float[NUM_BUTTONS];
    
    for(int i = 0 ; i < components.length; ++i) {
    	defaultValues[i] = components[i].getPollData();
    }
    
    setControllerValues(type);
  }
  
  /*************************************************
   * Helper method to put sleep and error handling
   * in one place
   * 
   * @param ms - number of milliseconds to sleep
   *************************************************/
  void sleep(int ms){
    try{
      Thread.sleep(ms);
    }
    catch(Exception e){
      //TODO log something
      System.out.println(e.toString());
    }
  }
  
  /************************************************
   * This method is used for debugging. It will print
   * out the index and value of any button that has
   * changed from it's default state
   ************************************************/
  public void printAllButtonStates(){
    controller.poll();
    Component[] comps = controller.getComponents();
    boolean somethingPrinted = false;
    for(int i = 0 ; i < comps.length ; ++i){
    	float temp = comps[i].getPollData();
    	if(temp != defaultValues[i]){
    		System.out.print("Button " + i + " w/ value " + temp + " , ");
    		somethingPrinted = true;
    	}
    }
    if(somethingPrinted){
    	System.out.println("");
    }
  }
  
  
 /*************************************************
  * This method is a helper to wait for the next
  * button pressed. It is used for setting up the
  * button mapping
  * 
  * @return button index of button pressed
  ************************************************/
  void setNextButton(PlayerButton buttonToSet){
  	int index = buttonToSet.index;
  	boolean buttonSet = false;
    while(!buttonSet){
      controller.poll();
      Component[] comps = controller.getComponents();
      
      for(int i = 0 ; i < comps.length ; ++i){
        float value = comps[i].getPollData();
        if(value != defaultValues[i]){
        	//Button Pressed so store component and value when pressed
        	pressedValues[index] = value;
        	buttonToComponentMap[index] = i;
        	buttonSet = true;
        	
        	System.out.println("Set button: " + buttonToSet + " aka component: " + i + " to value " + value);
        	
          //wait until they release the button
          while(comps[i].getPollData() == value){ 
            sleep(50);
            controller.poll();
          }
          break;
        }
      }
      
      //Nothing pressed. No need to spin the wheels. Wait 50ms
      sleep(50);
    }
  }
  
  @Override
  public void setLeftButton() {
  	setNextButton(PlayerButton.LEFT);
  }

  @Override
  public void setRightButton() {
  	setNextButton(PlayerButton.RIGHT);
  }

  @Override
  public void setDownButton() {
    setNextButton(PlayerButton.DOWN);
  }
  
  @Override
  public void setUpButton() {
    setNextButton(PlayerButton.UP);
  }

  @Override
  public void setJumpButton() {
  	setNextButton(PlayerButton.JUMP);
  }

  @Override
  public void setRunButton() {
  	setNextButton(PlayerButton.RUN);
  }
  

  @Override
  public void setPauseButton() {
    setNextButton(PlayerButton.PAUSE);
  }
  
  /***************************************************
   * Helper method to determine if a button is pressed
   * @param button - the button being tested
   * @return true if the button is pressed
   ****************************************************/
  boolean isPressed(PlayerButton button){
  	int componentIndex = buttonToComponentMap[button.index];
    return components[componentIndex].getPollData() == pressedValues[button.index];
  }

  @Override
  public int getDirection() {
  	if(isPressed(PlayerButton.RIGHT)){
      return (isPressed(PlayerButton.LEFT)) ?  0 : 1;
    }
    else{
      return (isPressed(PlayerButton.LEFT)) ? -1 : 0;
    }
  }

  @Override
  public boolean isDown() {
    return isPressed(PlayerButton.DOWN);
  }
  
  @Override
  public boolean isUp() {
    return isPressed(PlayerButton.UP);
  }

  @Override
  public boolean isJumping() {
  	return isPressed(PlayerButton.JUMP);
  }

  @Override
  public boolean isRunning() {
  	return isPressed(PlayerButton.RUN);
  }
  
  @Override
  public boolean isPaused() {
    return isPressed(PlayerButton.PAUSE);
  }
  
  @Override
  public void poll(){
  	controller.poll();
  }

  @Override
  public boolean isConnected() {
    // TODO - How to tell if game pad is connected?
    return isConnected;
  }
  
  @Override
  public void release() {}


  @Override
  public boolean isActionPressed() {
    // TODO - Assign action/powerup button (or would be just use run button?)
    return false;
  }
}

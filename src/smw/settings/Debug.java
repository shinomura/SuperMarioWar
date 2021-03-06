package smw.settings;

import java.io.File;
import smw.world.Structures.WorldBuffer;

/**
 * Provides flags to indicate whether a given function/area of code is being debugged.
 * This includes enabling logging and disabling certain features of the game.
 * There are also some miscellaneous utilities in this class.
 */
public class Debug {
  // Debug settings.
  public static final boolean LOG_FRAMERATE = false;
  public static final boolean LOG_WORLD_INFO = true;
  public static final boolean LOG_TILE_TYPE_INFO = false;
  public static boolean PLAYER_DEATH_IDLE = false;
  public static boolean PLAYER_DEATH_OFFSCREEN_TIMER = false;
  public static final boolean WINNER_TEXT = false;
  
  //FUN STUFF!!!!
  public static boolean CLIP_MODE = false;
  public static boolean CLIP_SHAPE_RECTANGLE = true;
  public static boolean CLIP_SHAPE_KEEP_ASPECT_RATIO = false;
  //Zooms in (instead of just showing a spotlight around the screen). 
  //Currently is only configured for 1 player but maybe do a splitscreen for more?
  public static boolean CLIP_ZOOM_STRETCH = true;
  
  /** Prints the map name, version, and tilesets */
  public static void printAllMapsAndVersions(){
    File folder = new File(Debug.class.getClassLoader().getResource("map/").getFile());
    File[] listOfFiles = folder.listFiles();
    for(File f : listOfFiles){
      String name = f.getName();
      if(name.endsWith(".map")){
        System.out.println(name + ": " + getMapVersion(name));
      }
    }
  }
  
  /**
   * This is a helper method to print all the map information. It skips
   * through the buffer to the information its looking for. For more
   * information, see the Level class on how a map is actually loaded
   */
  public static String getMapVersion(String name){
    String result = "";
    try {
      WorldBuffer buffer = new WorldBuffer(name);
      int version = buffer.getVersion();
      
      result += "Version: " + version;

      // For now only support latest map files (1.8+)
      if (version >= 1800) {

        for (int i = 0; i < 12; i++) {
          buffer.getInt();
        }
        
        buffer.getInt(); // unused 32 bits after auto filter section
                
        // Load tile set information.
        final int tileSetCount = buffer.getInt();

        result += " with tilesets: ";
        for (int i = 0; i < tileSetCount; i++) {
          buffer.getInt(); //Not used
          buffer.getString();
        }
      }
      buffer.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result;
  }

}

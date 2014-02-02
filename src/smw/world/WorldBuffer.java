package smw.world;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import smw.world.MovingPlatform.EllipticalPath;
import smw.world.MovingPlatform.Path;
import smw.world.MovingPlatform.StraightContinuousPath;
import smw.world.MovingPlatform.StraightSegmentPath;

public class WorldBuffer {

  MappedByteBuffer buffer;
  FileChannel fileChannel;
  RandomAccessFile file;
  
  public WorldBuffer(String worldName) throws Exception{
    file = new RandomAccessFile(this.getClass().getClassLoader().getResource("map/" + worldName).getPath().replaceAll("%20", " "), "r");
    fileChannel = file.getChannel();
    buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    buffer.order(ByteOrder.LITTLE_ENDIAN); // Java defaults to BIG_ENDIAN, but MAP files were probably made on a x86 PC.
    buffer.load();
  }
  
  public void close() throws IOException{
    buffer.clear();
    fileChannel.close();
    file.close();
  }
  
  public boolean getAutoFilter() {
    return buffer.getInt() > 0;
  }
    
  public boolean getBoolean(){
    return (buffer.get() != 0);
  }
  
  public byte getByte(){
    return buffer.get();
  }
  
  public char getChar(){
    return (char)buffer.get();
  }
  
  public DrawArea getDrawArea(){
    DrawArea drawArea = new DrawArea();
    
    drawArea.x = getInt();//TODO this might have to be forced signed
    drawArea.y = getInt();//TODO this might have to be forced signed
    drawArea.w = getInt();
    drawArea.h = getInt();
    
    return drawArea;
  }
  
  public FlagBaseLocation getFlagBaseLocation() {
    FlagBaseLocation flagBaseLocation = new FlagBaseLocation();
    
    flagBaseLocation.x = getShort();
    flagBaseLocation.y = getShort();
    
    return flagBaseLocation;
  }
  
  public float getFloat() {
    return buffer.getFloat();
  }

  public Hazard getHazard(){
    Hazard hazard = new Hazard();
    hazard.type = getShort();
    hazard.x    = getShort();
    hazard.y    = getShort();

    for(short j = 0; j < Hazard.NUMMAPHAZARDPARAMS; j++){
      hazard.iparam[j] = getShort();
    }
    
    for(short j = 0; j < Hazard.NUMMAPHAZARDPARAMS; j++){
      hazard.dparam[j] = getFloat();
    }
    
    return hazard;
  }
  
  public int getInt(){
    return buffer.getInt();
  }
  
  public Item getItem(){
    Item item = new Item();
    
    item.type = Tile.getType(getInt());
    item.x    = getInt();
    item.y    = getInt();
    
    return item;
  }
  
  public RaceGoalLocation getRaceGoalLocation() {
    RaceGoalLocation raceGoalLocation = new RaceGoalLocation();
    
    raceGoalLocation.x = getShort();
    raceGoalLocation.y = getShort();
    
    return raceGoalLocation;
  }
  
  public short getShort() {
    return (short)buffer.getInt();
  }
  
  public SpawnArea getSpawnArea(){
    SpawnArea spawnArea = new SpawnArea();
    
    spawnArea.left   = getShort();
    spawnArea.top    = getShort();
    spawnArea.width  = getShort();
    spawnArea.height = getShort();
    spawnArea.size   = getShort();

    return spawnArea;
  }

  public SpecialTile getSpecialTile() {
    short type =  getShort();
    return new SpecialTile(type);
  }
  
  public String getString() {
    final int length = getInt();
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < length ; ++i) {
      char c = getChar();
      if(c != 0){
        stringBuilder.append(c);
      }
    }
    
    return stringBuilder.toString();
  }
  
  public Tile getTile(int x, int y){
    Tile tile = new Tile(x*Tile.SIZE, y*Tile.SIZE);
    
    tile.ID              = (int)(buffer.get());
    tile.tileSheetColumn = (int)(buffer.get());
    tile.tileSheetRow    = (int)(buffer.get());

    return tile;
  }
 
  public int getVersion() {
    int version = 0;
    for (int i = 0; i < 4; i++) {
      version = 10*version + buffer.getInt();
    }
    
    return version;
  }
  
  public Warp getWarp() {
    Warp warp = new Warp();
    
    warp.direction  = getShort();
    warp.connection = getShort();
    warp.id         = getShort();
    
    return (warp.id != -1) ? warp : null;
  }
  
  public WarpExit getWarpExit(){
    WarpExit warpExit = new WarpExit();
    
    warpExit.direction = getShort();
    warpExit.connection = getShort();
    
    warpExit.id = getShort();
    warpExit.x  = getShort();
    warpExit.y  = getShort();

    warpExit.lockx = getShort();
    warpExit.locky = getShort();
    warpExit.warpx = getShort();
    warpExit.warpy = getShort();
    
    warpExit.numblocks = getShort();
    
    return warpExit;
  }
  
  public Path getPath(int type, int width, int height){
    Path result = null;
    
    ////////////////////////////////////////////////////////////////////////////////////
    //For some reason, original maps use a start/end based on the center of the object.
    //In the case of a straight path, it makes no sense so correcting it here
    int xOffset = (int)(width*Tile.SIZE)/2;
    int yOffset = (int)(height*Tile.SIZE)/2;
    
    if(type == 0){
      float startX = getFloat();
      float startY = getFloat();
      float endX = getFloat();
      float endY = getFloat();
      float velocity = getFloat();
      
      result = new StraightSegmentPath(velocity, startX - xOffset, startY - yOffset, endX - xOffset, endY - yOffset);
    }
    else if(type == 1){
      float startX = getFloat();
      float startY = getFloat();
      float angle = getFloat();
      float velocity = getFloat();
  
      result = new StraightContinuousPath(velocity, startX - xOffset, startY - yOffset, angle);
    }
    else if(type == 2){
      float radiusX = getFloat();
      float radiusY = getFloat();
      float centerX = getFloat();
      float centerY = getFloat();
      float angle   = getFloat();
      float velocity = getFloat();
  
      result = new EllipticalPath(velocity, angle, radiusX, radiusY, centerX, centerY);
    }
    
    return result;
  }
}
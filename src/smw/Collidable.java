package smw;

import smw.entity.Player;
import smw.ui.screen.GameFrame;
import smw.world.Tile;
import smw.world.Tile.TileType;

//TODO mk as other objects are implemented, might want to replace Player with something generic
//     ALSO this should probably be an abstract type
public abstract class Collidable {
  //TODO mk i hate this enough to leave it as is to draw attention to itself for being awful
  //Converts the tile type into the flags that this tile carries (solid + ice + death, etc)
  static final short[] g_iTileTypeConversion = {0, 1, 2, 5, 121, 9, 17, 33, 65, 6, 21, 37, 69, 3961, 265, 529, 1057, 2113, 4096};
  
  protected int left;
  protected int right;
  protected int top;
  protected int bottom;
  
  public int x, y;
  
  public TileType type;//TODO this should be deleted. Keeping it for moving platforms for now
  
  public Collidable(int x, int y) {
    
    this.x = x;
    this.y = y;
    
    left   = (x - Tile.SIZE + GameFrame.res_width) % GameFrame.res_width;
    right  = (x + Tile.SIZE) % GameFrame.res_width;
    top    = (y - Tile.SIZE) % GameFrame.res_height;
    bottom = (y + Tile.SIZE) % GameFrame.res_height;
  }

  /** by default, non-solid **/
  public int collideWithLeft(Player player, int newX){
    return newX;
  }
  
  /** by default, non-solid **/
  public int collideWithRight(Player player, int newX){
    return newX;
  }
  
  /** by default, non-solid **/
  public int collideWithTop(Player player, int newY){
    return newY;
  }

  /** by default, non-solid **/
  public int collideWithBottom(Player player, int newY){
    return newY;
  }
  
  public static Collidable getCollideable(int type, int x, int y){
    switch(type) {
      case 0:  return new NonSolid(x, y);
      case 1:  return new Solid(x, y);
      case 2:  return new SolidOnTop(x, y);
      //case 3: result = TileType.ICE; break;
      case 4:  return new Death(x, y);
      case 5:  return new DeathOnTop(x, y);
      case 6:  return new DeathOnBottom(x, y);
      case 7:  return new DeathOnLeft(x, y);
      case 8:  return new DeathOnRight(x, y);
      //case 9: result = TileType.ICE_ON_TOP; break;
      case 10: return new IceDeathOnBottom(x, y);
      case 11: return new IceDeathOnLeft(x, y);
      case 12: return new IceDeathOnRight(x, y);
      case 13: return new SuperDeath(x, y);
      case 14: return new SuperDeathOnTop(x, y);
      case 15: return new SuperDeathOnBottom(x, y);
      case 16: return new SuperDeathOnLeft(x, y);
      case 17: return new SuperDeathOnRight(x, y);
      //case 18: result = TileType.PLAYER_DEATH; break;
      //case 19: result = TileType.GAP; break;
    }
    
    return new NonSolid(x, y);
  }
  
  /**
   * Non Solid
   */
  public static class NonSolid extends Collidable{
    public NonSolid(int x, int y) {
      super(x, y);
      type = Tile.TileType.NONSOLID;
    }
  }
  
  /**
   * Solid
   */
  public static class Solid extends Collidable{
    
    public Solid(int x, int y) {
      super(x, y);
      type = Tile.TileType.SOLID;
    }

    @Override
    public int collideWithLeft(Player player, int newX){
      player.physics.collideWithWall();
      return left;
    }
    
    @Override
    public int collideWithRight(Player player, int newX){
        player.physics.collideWithWall();
        return right;
      }
      
    @Override
    public int collideWithTop(Player player, int newY){
      player.physics.collideWithFloor();
      return top;
    }
      
    @Override
    public int collideWithBottom(Player player, int newY){
      player.physics.collideWithCeiling();
      return bottom;
    }
  }
  
  /**
   * SolidOnTop
   */
  public static class SolidOnTop extends Collidable{
    public SolidOnTop(int x, int y) {
      super(x, y);
      type = Tile.TileType.SOLID_ON_TOP;
    }
    
    @Override
    public int collideWithTop(Player player, int newY){
      if(!player.pushedDown && player.physics.playerControl.isDown()) {
        // If this is the first time we reached this then the player pushed down the first time to fall through.
        // Set the falling through flags and height.
        if (!player.isFallingThrough) {
          player.isFallingThrough = true;
          player.fallHeight = player.y;
          player.pushedDown = true;
        }
      }
      else{
        player.physics.collideWithFloor();
        return top;
      }
      
      return newY;
    }
  }
  
  /**
   * Death
   */
  public static class Death extends Solid{
    public Death(int x, int y) {
      super(x, y);
      type = Tile.TileType.DEATH;
    }
    
    @Override
    public int collideWithTop(Player player, int newY){
      player.death();
      return newY;
    }
    
    @Override
    public int collideWithBottom(Player player, int newY){
      player.death();
      return newY;
    }
    
    @Override
    public int collideWithRight(Player player, int newX){
      player.death();
      return newX;
    }
    
    @Override
    public int collideWithLeft(Player player, int newX){
      player.death();
      return newX;
    }
  }
  
  /**
   * Death On Top
   */
  public static class DeathOnTop extends Solid{
    public DeathOnTop(int x, int y) {
      super(x, y);
      type = Tile.TileType.DEATH_ON_TOP;
    }
    
    @Override
    public int collideWithTop(Player player, int newY){
      player.death();
      return newY;
    }
  }
  
  /**
   * Death On Bottom
   */
  public static class DeathOnBottom extends Solid{
    public DeathOnBottom(int x, int y) {
      super(x, y);
      type = Tile.TileType.DEATH_ON_BOTTOM;
    }
    
    @Override
    public int collideWithBottom(Player player, int newY){
      player.death();
      return newY;
    }
  }
  
  /**
   * Death On Right
   */
  public static class DeathOnRight extends Solid{
    public DeathOnRight(int x, int y) {
      super(x, y);
      type = Tile.TileType.DEATH_ON_RIGHT;
    }
    
    @Override
    public int collideWithRight(Player player, int newX){
      player.death();
      return newX;
    }
  }
  
  /**
   * Death On Left
   */
  public static class DeathOnLeft extends Solid{
    public DeathOnLeft(int x, int y) {
      super(x, y);
      type = Tile.TileType.DEATH_ON_LEFT;
    }
    
    @Override
    public int collideWithLeft(Player player, int newY){
      player.death();
      return newY;
    }
  }
  
  /**
   * Super Death
   */
  public static class SuperDeath extends Solid{
    public SuperDeath(int x, int y) {
      super(x, y);
      type = Tile.TileType.SUPER_DEATH;
    }
    
    @Override
    public int collideWithTop(Player player, int newY){
      player.superDeath();
      return newY;
    }
    
    @Override
    public int collideWithBottom(Player player, int newY){
      player.superDeath();
      return newY;
    }
    
    @Override
    public int collideWithRight(Player player, int newX){
      player.superDeath();
      return newX;
    }
    
    @Override
    public int collideWithLeft(Player player, int newX){
      player.superDeath();
      return newX;
    }
  }
  
  /**
   * Super Death On Top
   */
  public static class SuperDeathOnTop extends Solid{
    public SuperDeathOnTop(int x, int y) {
      super(x, y);
      type = Tile.TileType.SUPER_DEATH_TOP;
    }
    
    @Override
    public int collideWithTop(Player player, int newY){
      player.superDeath();
      return newY;
    }
  }
  
  /**
   * Super Death On Bottom
   */
  public static class SuperDeathOnBottom extends Solid{
    public SuperDeathOnBottom(int x, int y) {
      super(x, y);
      type = Tile.TileType.SUPER_DEATH_BOTTOM;
    }
    
    @Override
    public int collideWithBottom(Player player, int newY){
      player.superDeath();
      return newY;
    }
  }
  
  /**
   * Super Death On Right
   */
  public static class SuperDeathOnRight extends Solid{
    public SuperDeathOnRight(int x, int y) {
      super(x, y);
      type = Tile.TileType.SUPER_DEATH_RIGHT;
    }
    
    @Override
    public int collideWithRight(Player player, int newX){
      player.superDeath();
      return newX;
    }
  }
  
  /**
   * Super Death On Left
   */
  public static class SuperDeathOnLeft extends Solid{
    public SuperDeathOnLeft(int x, int y) {
      super(x, y);
      type = Tile.TileType.SUPER_DEATH_LEFT;
    }
    
    @Override
    public int collideWithLeft(Player player, int newY){
      player.superDeath();
      return newY;
    }
  }
  
  /**
   * Ice Death On Bottom
   */
  public static class IceDeathOnBottom extends Solid{
    public IceDeathOnBottom(int x, int y) {
      super(x, y);
      type = Tile.TileType.ICE_DEATH_ON_BOTTOM;
    }
    
    @Override
    public int collideWithBottom(Player player, int newY){
      player.death(); //TODO verify this is correct (not super death or need its own) AND right/left same issue (next 2 classes)
      return newY;
    }
  }
  
  /**
   * Ice Death On Right
   */
  public static class IceDeathOnRight extends Solid{
    public IceDeathOnRight(int x, int y) {
      super(x, y);
      type = Tile.TileType.ICE_DEATH_ON_RIGHT;
    }
    
    @Override
    public int collideWithRight(Player player, int newX){
      player.death();
      return newX;
    }
  }
  
  /**
   * Ice Death On Left
   */
  public static class IceDeathOnLeft extends Solid{
    public IceDeathOnLeft(int x, int y) {
      super(x, y);
      type = Tile.TileType.ICE_DEATH_ON_LEFT;
    }
    
    @Override
    public int collideWithLeft(Player player, int newY){
      player.superDeath();
      return newY;
    }
  }
}

class StraightSegmentPath{
  
  int X = 0;
  int Y = 1;
  
  float[] currentPos = new float[2];
  float[] velocity   = new float[2];
  
  int[] startPos = new int[2];
  int[] endPos   = new int[2];
  
  public StraightSegmentPath(float velocity, int startX, int endX, int startY, int endY){
    this.currentPos[X] = (float) startX;
    this.currentPos[Y] = (float) startY;
    
    //Calculate velocities for X/Y
    int xLength = endX - startX;
    int yLength = endY - startY;
    
    float velocityLength = Math.sqrt(xLength*xLength - (yLength*yLength));
    double angle = Math.tan(((float)yLength)/xLength);
    
    velocity[X] = velocityLength*Math.acos(angle);
    velocity[Y] = velocityLength*Math.asin(angle);
    
    //Make the "start" the "lesser" one
    if(startX < endX){
      this.startPos[X] = startX;
      this.endPos[X]   = endX;
    }
    else{
      velocity[X] = (-1)*velocity[X];
      this.startPos[X] = endX;
      this.endPos[X]   = startX;
    }

    //Make the "start" the "lesser" one
    if(startY < endY){
      this.startPos[Y] = startY;
      this.endPos[Y]   = endY;
    }
    else{
      velocity[Y] = (-1)*velocity[Y];
      this.startPos[Y] = endY;
      this.endPos[Y]   = startY;
    }
  }
  
  public void move(long timeDif){
    move(X, timeDif);
    move(Y, timeDif);
  }
  
  public int getX(){
    return get(X);
  }
  
  public int getY(){
    return get(Y);
  }
  
  int get(int i){
    return (int) current[i];
  }
  
  void move(int i, timeDif){
    
    current[i] += velocity[i]*timeDif;
    
    //////////////////////////////////////////////
    //Make sure we didn't go beyond the start/end
    if(current[i]> end[i]){
      //////////////////////////////////////////////////
      //we really want (end - how far we went beyond end) 
      //             = (end - (current - end) 
      //             = 2*end - current
      current[i]  = 2*end[i] - current[i];
      velocity[i] = (-1)*velocity[i];
    }
    else if(current[i] < start[i]){
      //////////////////////////////////////////////////
      //we really want (start + how far we went beyond start) 
      //             = (start + (start - current) 
      //             = 2*end - current
      current[i]  = 2*start[i] - current[i];
      velocity[i] = (-1)*velocity[i];
    }
  }
}

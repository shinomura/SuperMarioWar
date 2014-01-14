package ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import model.Player;

//This is a temporary implementation to just get a block moving
public class PlayerControl extends KeyAdapter{
	Player player;
	
	int left;
	int right;
	int up;
	int down;
	int jump;

	final int VELOCITY = 10;
	final int NEGATIVE_VELOCITY = -10;
	
	public PlayerControl(Player player){
		this.player = player;
		
		//TODO these are just temp for testing (or default?)
		left = KeyEvent.VK_LEFT;
		right = KeyEvent.VK_RIGHT;
		up = KeyEvent.VK_UP;
		down = KeyEvent.VK_DOWN;
		jump = KeyEvent.VK_UP;
	}
	
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		//TODOD this (and on keypressed) propose an interesting question of how to handle opposing inputs
        if(key == left){
        	player.stopMovingLeft();
        	return;
        }
	
		if(key == right){
	    	player.stopMovingRight();
	    	return;
	    }
    }

    public void keyPressed(KeyEvent e) {
    	int key = e.getKeyCode();

        if(key == left){
        	player.moveLeft();
        	return;
        }
        
        if(key == right){
        	player.moveRight();
        	return;
        }
        
        if(key == jump){
        	//TODO need to only be able to jump once 
        	player.jump();
        }
    }
}

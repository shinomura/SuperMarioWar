package ui.screen;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;

import javax.swing.JFrame;

import model.Player;

public class GameFrame extends JFrame{
	//TODO this should be in a common area
	public static final String Title = "Super Mario War";
	
	//TODO this is REALLY hacky but i just wanted to get the stupid thing to work
	Player[] players;
	
	public GameFrame(Player[] players){
		add(new GamePanel());
	    setTitle(Title);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setSize(800, 600); //TODO this should probably be pulled out
	    setLocationRelativeTo(null);
	    setVisible(true);
	    setResizable(false);
	    
	    this.players = players;
	}

    @Override
    public void paint(Graphics g) {
    // Get buffer strategy and loop to protect against lost frames.
    BufferStrategy bs = getBufferStrategy();
    do {
      try {
        g = bs.getDrawGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        Graphics2D g2d = (Graphics2D)g;
        if (players != null && players.length > 0) {
          for(Player p : players){
            g2d.drawImage(p.getImage(), p.getX(), p.getY(), this);
          }
        }
      } finally {
        // Free up graphics.
        g.dispose();
      }
      // Display contents of buffer.
      bs.show();
    } while (bs.contentsLost());   
  }
}

package mazewar;

import java.io.Serializable;

public class MazeWarPkt implements Serializable {

	/* define key pressed action */
	public static final int MAZEWAR_FORWARD = 101;
	public static final int MAZEWAR_BACKWARD   = 102;
	public static final int MAZEWAR_LEFT   = 103;
	public static final int MAZEWAR_RIGHT = 104;
	public static final int MAZEWAR_FIRE = 105;
	public static final int MAZEWAR_QUIT     = 199;
	
	/* error codes */
	
	/* the event is stored here*/
	public int event;
	
	/* the player id */
	public int player;
	
}
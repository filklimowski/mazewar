import java.io.Serializable;

public class MazeWarPkt implements Serializable {

	/* define key pressed action */
	public static final int MAZEWAR_FORWARD = 101;
	public static final int MAZEWAR_BACKWARD   = 102;
	public static final int MAZEWAR_LEFT   = 103;
	public static final int MAZEWAR_RIGHT = 104;
	public static final int MAZEWAR_FIRE = 105;
    public static final int MAZEWAR_COORDINATES = 121;
	public static final int MAZEWAR_QUIT     = 199;
    public static final int MAZEWAR_SPAWN = 120;
    public static final int MAZEWAR_TICK = 200;
	
	/* error codes */
	
	/* the event is stored here*/
	public int event;
	
	/* the player id */
	public int player;

    public String playerName;

    public int spawnX, spawnY;

    public MazeWarPkt(int event, int player, String playerName) {
        this.event = event;
        this.player = player;
        this.playerName = playerName;
    }

    public MazeWarPkt(int event, int player, String playerName, int spawnX, int spawnY) {
        this.event = event;
        this.player = player;
        this.playerName = playerName;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

	
}
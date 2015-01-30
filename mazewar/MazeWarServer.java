import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;

public class MazeWarServer {
	
	public static Queue<MazeWarPkt> eventQ = new LinkedList<MazeWarPkt>();

	public static void main(String[] args) throws IOException {
		


		//Error checking
		//Declare Data Structures
		
		ServerSocket serverSocket = new ServerSocket(10000);
		boolean listening = true;
		new MazeWarBcastThread().start();
		
		while(listening) {
			//listens and enqueues
			new MazeWarThread(serverSocket.accept()).start(); 
		}
	}
}


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MazeWarServer {
	
	private LinkedBlockingQueue<MazeWarPkt> eventQ;
	private ArrayList<ConnectionToClient> clientList;
	private ServerSocket serverSocket;

	public MazeWarServer(int port) { {
		
		//Error checking?
		
		clientList = new ArrayList<ConnectionToClient>();
        eventQ = new LinkedBlockingQueue<MazeWarPkt>();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//boolean listening = true;
		//new MazeWarBcastThread().start();
		
		Thread accept = new Thread() {
            public void run(){
                while(true){
                    try{
                        Socket s = serverSocket.accept();
			System.out.println("Accepted connection\n");
			
                        clientList.add(new ConnectionToClient(s));
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };
        
        accept.setDaemon(true);
        accept.start();

        Thread messageHandling = new Thread() {
            public void run(){
                while(true){
                    try{
                        MazeWarPkt eventPkt = eventQ.take();
                        // dequeue and send to all clients
                        sendToAll(eventPkt);
                        System.out.println("Broadcasting: " + eventPkt.event + " from Player: " + eventPkt.player);
                    }
                    catch(InterruptedException e){ }
                }
            }
        };

        messageHandling.setDaemon(true);
        messageHandling.start();
    };	
}

    private class ConnectionToClient {

	private Socket socket = null;
	private ObjectInputStream fromClient = null;
	private ObjectOutputStream toClient = null;

        ConnectionToClient(Socket socket) throws IOException {
            	this.socket = socket;
		this.fromClient = new ObjectInputStream(socket.getInputStream());
		/* stream to write back to client */
		this.toClient = new ObjectOutputStream(socket.getOutputStream());

            Thread read = new Thread(){
                public void run(){
                    while(true){
                        try{
				MazeWarPkt packetFromClient = (MazeWarPkt) fromClient.readObject();
				eventQ.put(packetFromClient);
				System.out.println("Received and Enqueued " + 
				packetFromClient.event + " from Player: " + packetFromClient.player);

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(IOException e){ 
				e.printStackTrace(); 
			}
                    }
                }
            };

            read.setDaemon(true); // terminate when main ends
            read.start();
        }

        public void write(MazeWarPkt eventPkt) {
            try{
                toClient.writeObject(eventPkt);
		System.out.println("Sent back to client\n");
            }
            catch(IOException e){ e.printStackTrace(); }
        }
    }

    public void sendToOne(int index, MazeWarPkt eventPkt) throws IndexOutOfBoundsException {
        clientList.get(index).write(eventPkt);
    }

    public void sendToAll(MazeWarPkt eventPkt){
        for(ConnectionToClient client : clientList)
            client.write(eventPkt);
    }

}



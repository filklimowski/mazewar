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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MazeWarServer {
	
	private LinkedBlockingQueue<MazeWarPkt> eventQ;
	private ArrayList<ConnectionToClient> clientList;
    private ArrayList<ConnectionToPeer> peerList;
	private ServerSocket serverSocket;
    private Lock acceptLock;
    int numConnections = 0;



	public MazeWarServer(int port) {

        acceptLock = new ReentrantLock();

        //Error checking?

        clientList = new ArrayList<ConnectionToClient>();
        peerList = new ArrayList<ConnectionToPeer>();
        eventQ = new LinkedBlockingQueue<MazeWarPkt>();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        this.listen();

    }

    public MazeWarServer(int port, String hostname, int port2) {
        acceptLock = new ReentrantLock();
        Socket commPeer = null;
        ObjectOutputStream out;
        try {
            commPeer = new Socket(hostname, port2);
            out = new ObjectOutputStream(commPeer.getOutputStream());
            serverSocket = new ServerSocket(port);
            MazeWarPkt p = new MazeWarPkt(MazeWarPkt.MAZEWAR_REQ_PEERS, -1, "Server");
            out.writeObject(p);

        }
        catch (IOException err) {
            System.out.println("Exception occurred");
        }
        listen();
    }

    private void listen() {
        //boolean listening = true;
        Thread missileTick = new Thread() {
            public void run() {
                MazeWarPkt tickPkt = new MazeWarPkt(MazeWarPkt.MAZEWAR_TICK, -1, "Server");
                while (true) {
                    try {
                        eventQ.put(tickPkt);
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        missileTick.start();
            	
		
		Thread accept = new Thread() {
            public void run(){
                while(true){
                    try{
                        acceptLock.lock();
                        Socket s = serverSocket.accept();
                        System.out.println("Accepted connection\n");
                        clientList.add(new ConnectionToClient(s));
                        MazeWarServer.this.numConnections++;
                    }
                    catch(IOException e){ e.printStackTrace(); }
                    finally {
                        acceptLock.unlock();
                    }
                }
            }
        };
        
        accept.start();

        while(true) {
            acceptLock.lock();
            if (numConnections > 0) {
                acceptLock.unlock();
                break;
            }
            else {
                acceptLock.unlock();
            }
        }

        Thread acceptPeers = new Thread() {
            public void run(){
                while(true){
                    try{
                        Socket s = serverSocket.accept();
                        System.out.println("Accepted connection\n");
                        peerList.add(new ConnectionToPeer(s));

                        //TODO: get hostname and port of new peer to be stored in connection info
                        MazeWarServer.this.numConnections++;
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };

        accept.start();

        Thread broadcastMessages = new Thread() {
            public void run(){
                while(true){
                    try{
                        MazeWarPkt eventPkt = eventQ.take();
                        
                        if(eventPkt.event == MazeWarPkt.MAZEWAR_REQ_PEERS) {
                            MazeWarPkt peerListPkt = new MazeWarPkt(MazeWarPkt.MAZEWAR_PEER_LIST, eventPkt.player, eventPkt.playerName);
                            peerListPkt.peerHosts = new ArrayList<String>();
                            peerListPkt.peerPorts = new ArrayList<Integer>();

                            for (ConnectionToPeer peer: peerList) {
                                peerListPkt.peerHosts.add(peer.hostname);
                                peerListPkt.peerPorts.add(peer.port);
                            }
                        }
                        else if(eventPkt.event == MazeWarPkt.MAZEWAR_CLIENT_LIST_REQ) {
                        	//send clientList
                        	int requestingClient=-1;
                        	MazeWarPkt clientListPkt = new MazeWarPkt(MazeWarPkt.MAZEWAR_CLIENT_LIST_RESP, eventPkt.player, eventPkt.playerName);
                        	clientListPkt.clientList = new ArrayList<ClientInfo>();
                        	for(int i=0; i<clientList.size(); i++) {
                        		ConnectionToClient client = clientList.get(i);
                        		if(client.player != eventPkt.player) { //we want to add to list all clients other than the one requesting
                                	ClientInfo clientInfo = new ClientInfo(client.player, client.playerName, client.spawnX, client.spawnY, client.spawnD);
                                	clientListPkt.clientList.add(clientInfo);
                        		}
                        		else {
                        			requestingClient = i;
                        		}
                        	}
                        	sendToOne(requestingClient, clientListPkt);
                        }
                        else if(eventPkt.event == MazeWarPkt.MAZEWAR_PEER_LIST)
                        {
                            //TODO: build connection to all peers
                            System.out.println("Received Peer List");
                        }
                        else {
                        	// dequeue and send to all clients
                            sendToAll(eventPkt);
                            System.out.println("Broadcasting: " + eventPkt.event + " from Player: " + eventPkt.player);
                        }
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        broadcastMessages.start();
    }

    private class ConnectionToClient {

	private Socket socket = null;
	private ObjectInputStream fromClient = null;
	private ObjectOutputStream toClient = null;
	
	private int player = 0;
    private String playerName = null;
    
    private int spawnX = 0;
    private int spawnY = 0;
    private Direction spawnD = null;
    

        ConnectionToClient(Socket socket) throws IOException {
        	
            this.socket = socket;
		    this.fromClient = new ObjectInputStream(socket.getInputStream());
		    /* stream to write back to client */
		    this.toClient = new ObjectOutputStream(socket.getOutputStream());
		    
		    final ConnectionToClient thisClient = this;
		    
            Thread read = new Thread(){
                public void run(){
                    while(true){
                        try{
                            MazeWarPkt packetFromClient = (MazeWarPkt) fromClient.readObject();
                            eventQ.put(packetFromClient);
                            System.out.println("Received and Enqueued " +
                            packetFromClient.event + " from Player: " + packetFromClient.player);
                            
                            if(packetFromClient.event == MazeWarPkt.MAZEWAR_SPAWN) {
                            	thisClient.player = packetFromClient.player;
                            	System.out.println("connection to client playerid is: " + thisClient.player);
                            	thisClient.playerName = packetFromClient.playerName;
                            }
                            else if (packetFromClient.event == MazeWarPkt.MAZEWAR_COORDINATES) {
                            	thisClient.spawnX = packetFromClient.spawnX;
                            	thisClient.spawnY = packetFromClient.spawnY;
                            	thisClient.spawnD = packetFromClient.spawnD;
                            }

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


    private class ConnectionToPeer {

        private Socket socket = null;
        private ObjectInputStream fromPeer = null;
        private ObjectOutputStream toPeer = null;

        private int player = 0;
        private String playerName = null;

        private int spawnX = 0;
        private int spawnY = 0;
        private Direction spawnD = null;
        public String hostname;
        public int port;


        ConnectionToPeer(Socket socket) throws IOException {

            this.socket = socket;
            this.fromPeer = new ObjectInputStream(socket.getInputStream());
		    /* stream to write back to client */
            this.toPeer = new ObjectOutputStream(socket.getOutputStream());

            final ConnectionToPeer thisPeer = this;

            Thread read = new Thread(){
                public void run(){
                    while(true){
                        try{
                            MazeWarPkt packetFromClient = (MazeWarPkt) fromPeer.readObject();
                            eventQ.put(packetFromClient);
                            System.out.println("Received and Enqueued " +
                                    packetFromClient.event + " from Player: " + packetFromClient.player);

                            if(packetFromClient.event == MazeWarPkt.MAZEWAR_SPAWN) {
                                thisPeer.player = packetFromClient.player;
                                System.out.println("connection to client playerid is: " + thisPeer.player);
                                thisPeer.playerName = packetFromClient.playerName;
                            }
                            else if (packetFromClient.event == MazeWarPkt.MAZEWAR_COORDINATES) {
                                thisPeer.spawnX = packetFromClient.spawnX;
                                thisPeer.spawnY = packetFromClient.spawnY;
                                thisPeer.spawnD = packetFromClient.spawnD;
                            }

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

            read.start();
        }

        public void write(MazeWarPkt eventPkt) {
            try{
                toPeer.writeObject(eventPkt);
                System.out.println("Sent back to peer\n");
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

    public void sendToAllPeers(MazeWarPkt eventPkt) {
        for(ConnectionToPeer peer:peerList)
            peer.write(eventPkt);
    }

    public static void main(String args[]) {

        int port = 4444;
        String hostname = "localhost";
        int port2 = 4445;

        if(args.length == 1 ) {
            port = Integer.parseInt(args[0]);
            new MazeWarServer(port);
        }
        else if (args.length == 3) {
            port = Integer.parseInt(args[0]);
            hostname = args[1];
            port2 = Integer.parseInt(args[2]);
            new MazeWarServer(port, hostname, port2);
        } else {
            System.err.println("ERROR: Invalid arguments!");
            System.exit(-1);
        }

		/* Create the GUI */
    }

}



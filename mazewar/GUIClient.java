/*
Copyright (C) 2004 Geoffrey Alan Washburn
      
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
      
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
      
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
*/

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * An implementation of {@link LocalClient} that is controlled by the keyboard
 * of the computer on which the game is being run.  
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: GUIClient.java 343 2004-01-24 03:43:45Z geoffw $
 */

public class GUIClient extends LocalClient implements KeyListener {

        private Socket comm;
        private ObjectOutputStream out;
        private int playerId = (int)(Math.random() * 32000);
        /**
         *
         *
         * Create a GUI controlled {@link LocalClient}.  
         */
        public GUIClient(String name, Socket comm) {
            super(name);
            this.comm = comm;
            try {
                out = new ObjectOutputStream(comm.getOutputStream());
                MazeWarPkt p = new MazeWarPkt(MazeWarPkt.MAZEWAR_SPAWN, playerId, name);
                out.writeObject(p);
            }
            catch (IOException err){
                System.out.println("Exception occurred");
            }
        }
        
        /**
         * Handle a key press.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyPressed(KeyEvent e) {
                // If the user pressed Q, invoke the cleanup code and quit.
            try{
                if((e.getKeyChar() == 'q') || (e.getKeyChar() == 'Q')) {
                        //Mazewar.quit();
                    out = new ObjectOutputStream(comm.getOutputStream());
                    MazeWarPkt p = new MazeWarPkt(MazeWarPkt.MAZEWAR_QUIT, playerId, this.getName());
                    out.writeObject(p);

                    //send a MazeWarPkt with MAZEWAR_QUIT

                // Up-arrow moves forward.
                } else if(e.getKeyCode() == KeyEvent.VK_UP) {
                        //forward();
                    out = new ObjectOutputStream(comm.getOutputStream());
                    MazeWarPkt p = new MazeWarPkt(MazeWarPkt.MAZEWAR_FORWARD, playerId, this.getName());
                    out.writeObject(p);
                    //send a MazeWarPkt to the server with MAZEWAR_FORWARD

                // Down-arrow moves backward.
                } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                        //backup();
                    out = new ObjectOutputStream(comm.getOutputStream());
                    MazeWarPkt p = new MazeWarPkt(MazeWarPkt.MAZEWAR_BACKWARD, playerId, this.getName());
                    out.writeObject(p);
                    //send a MazeWarPkt to the server with MAZEWAR_BACKWARD
                        
                // Left-arrow turns left.
                } else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                        //turnLeft();
                    out = new ObjectOutputStream(comm.getOutputStream());
                    MazeWarPkt p = new MazeWarPkt(MazeWarPkt.MAZEWAR_LEFT, playerId, this.getName());
                    out.writeObject(p);
                    //send a MazeWarPkt to the server with MAZEWAR_LEFT

                // Right-arrow turns right.
                } else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        //turnRight();
                    out = new ObjectOutputStream(comm.getOutputStream());
                    MazeWarPkt p = new MazeWarPkt(MazeWarPkt.MAZEWAR_RIGHT, playerId, this.getName());
                    out.writeObject(p);
                    //send a MazeWarPkt to the server with MAZEWAR_RIGHT

                // Spacebar fires.
                } else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                        //fire();
                    out = new ObjectOutputStream(comm.getOutputStream());
                    MazeWarPkt p = new MazeWarPkt(MazeWarPkt.MAZEWAR_FIRE, playerId, this.getName());
                    out.writeObject(p);
                    //send a MazeWarPkt to the server with MAZEWAR_FIRE
                }
            }
            catch (IOException err) {
                System.out.println("Exception occurred");
            }
        }
        
        /**
         * Handle a key release. Not needed by {@link GUIClient}.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyReleased(KeyEvent e) {
        }
        
        /**
         * Handle a key being typed. Not needed by {@link GUIClient}.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyTyped(KeyEvent e) {
        }

}

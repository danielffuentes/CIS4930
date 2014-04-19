package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import backend.Cell;
import backend.Grid;
import backend.IterationCalculator;

public class ClientConnection extends Thread {
	private Socket s = null;
	private ObjectOutputStream oos = null;
	private ObjectInputStream ois = null;
	private volatile Grid g = null;
	private volatile boolean isPlaying = false;
	public boolean getIsPlaying() {
		return isPlaying;
	}
	
	public Grid getGrid() {
		return g;
	}
	private ArrayList<UICallback> subscribedUI = new ArrayList<UICallback>();
	public void subscribe(UICallback e) {
		subscribedUI.add(e);
	}
	public void unsubscribe(UICallback e) {
		subscribedUI.remove(e);
	}

    public void resizeGrid(int numRows) throws Exception {
        g = new Grid(numRows);
        send(g);
    }

	public ClientConnection(Socket s) {
		this.s = s;
		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void send(Object o) {
		try {
			oos.writeObject(o);
			oos.reset();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		boolean doLoop = true;
		try {
			Object rcvObj;
			while(doLoop) {
				rcvObj = ois.readObject();
				if(rcvObj != null) {
					if(rcvObj instanceof Grid) {
						g = (Grid) rcvObj;
						System.out.println("Received Cell Size: " + g.getNumRows());
						updateDisplays();
					}
					else if(rcvObj instanceof Cell) {
						Cell c = (Cell) rcvObj;
						g.setCell(c);
						System.out.println("Received Cell " + c + " " + ((c.getCellState() == 1) ? "alive":"dead"));
						updateCell(c);
					}
					else if(rcvObj instanceof Thread) {
						//AtomicReference[] list = (AtomicReference[]) rcvObj;
						Thread t = (Thread) rcvObj;
						t.start();
						//TODO: receive a Calculator extends Thread: create ConnectionCallback method, 
						//which the Calculator calls when it is done.
						//set the callback for a function that sends back the AtomicReference[]
						
						//TODO: figure out how to calculate using this
						//TODO: send back calculation to server
					}
					else if(rcvObj instanceof NetworkMessage) {
						NetworkMessage nm = (NetworkMessage) rcvObj;
						switch(nm) {
							case PLAY:
								System.out.println("Received PLAY");
								isPlaying = true;
								break;
							case PAUSE:
								System.out.println("Received PAUSE");
								isPlaying = false;
								break;
							case CLEAR://probably won't be used
								break;
							default:
								break;
						}
					}
					
				}
				else {
					doLoop = false;
				}
			}
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();//EOFException
		}
		finally {
			if(ois != null) {
				try {
					ois.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			if(oos != null) {
				try {
					oos.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			if(s != null) {
				try {
					s.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void updateDisplays() {
		for(UICallback uic : subscribedUI) {
			uic.updateGame();
		}
	}
	public void updateCell(Cell c) {
		for(UICallback uic : subscribedUI) {
			uic.updateCell(c);
		}
	}
	
	public void changeCellState(int x, int y, int state) {
		Cell c = null;
		try {
			c = new Cell(x,y);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(c != null) {
			c.setCellState(state);
		}
		System.out.println("Sent cell " + c + " " + ((state == 1) ? "alive":"dead") + " to server");
		send(c);
	}
	
	public void startLife(int x, int y){
        //courdinates of the cell that the user clicked to be the first cell
		Cell c = null;
		try {
			c = new Cell(x,y);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(c != null) {
			c.setCellState(1);
		}
		send(c);
    }
	
	public void stopLife(int x, int y) {
		Cell c = null;
		try {
			c = new Cell(x,y);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		send(c);
	}
	
	public void play() {
		//DON'T set isPlaying (let the receive set it)
		send(NetworkMessage.PLAY);
	}
	
	public void pause() {
		//DON'T set isPlaying (let the receive set it)
		send(NetworkMessage.PAUSE);
	}
	
	//For resizes, or clearing of the Grid
	public void newGame(Grid g) {
		send(g);
	}
	
	public void sendPartialComponent(ArrayList<Cell> component) {
		send(component);
	}

	
	public static void main(String[] args) {
		final String serverIP = "127.0.0.1";
		ClientConnection self;
		try {
			Socket clientSocket = new Socket(serverIP, Server.port);
			self = new ClientConnection(clientSocket);
			self.start();
			Thread.sleep(10000);
			self.send(NetworkMessage.PLAY);
			System.out.println("Client socket accepted");
			System.out.println("Created I/O streams");
		}
		catch(IOException ex) {
			System.out.println("Failed to create I/O streams");
			System.exit(1);
		} catch(InterruptedException ex){
            System.out.println("Failed to create connection");
        }
	}
    
}

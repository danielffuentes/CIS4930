package java.gameoflife;

public class Cell extends QuadTreeElement {
	int x, y;
	boolean isAlive;
	
	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void setLife(boolean isAlive) {
		this.isAlive = isAlive;
	}
}
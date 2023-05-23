package process;

import java.awt.image.BufferedImage;

public class Buffer {
	private BufferedImage image;
	private boolean available = false;
	private int length;				  //Multipla utilizare: -semnal pentru inceperea thread-ului WriteResult
									  //					-ofera dimensiunea unui chunk din imagine catre WriteResult
	
	public synchronized BufferedImage get() {
		while (!available) {
			try {
					wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		available = false;
		notifyAll();
		return image;
	}
	//Difera doar tipul de date returnat
	public synchronized int getInt() {
		while (!available) {
			try {
					wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		available = false;
		notifyAll();
		return length;
	}
	
	public synchronized void put (BufferedImage image) {	//Pentru trimiterea chunks de imagine
		while (available) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.image = image;
		available = true;
		notifyAll();
	}
	
	public synchronized void put (int length) {	//Pentru trimiterea catre WriteResult a dimensiunii img si a semnalului de start
		while (available) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.length = length;
		available = true;
		notifyAll();
	}
}

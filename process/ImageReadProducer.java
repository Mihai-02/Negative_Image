package process;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageReadProducer implements Runnable{
	private Buffer buffer;
	private Thread t;
	private ProcessInfo info;		//Pentru preluare path fisier de intrare si dimensiuni
	long producerStartTime;
	long producerEndTime;
	
	public ImageReadProducer(Buffer b, ProcessInfo info) {
		producerStartTime = System.nanoTime();	//Start Thread Producer
		buffer = b;
		this.info = info;
		t = new Thread(this, "Producer");
		t.start();
	}
	
	public void run() {
		try {	
			for(int i=1; i<=4; i++){
				//Alegerea dimensiunii unui chunk folosind un obiect Rectangle ce va selecta cate un sfert de imagine
				Rectangle sourceRegion;
				if(i != 4)
					//Se preia cate un sfert de imagine de sus in jos
					sourceRegion= new Rectangle(0, (info.inHeight*(i-1))/4, info.inWidth, info.inHeight/4);	
				else		//Ultima bucata poate fi mai mica decat restul(inaltimea imaginii nu e divizibila cu 4)
					sourceRegion= new Rectangle(0, (info.inHeight*3)/4, info.inWidth, info.inHeight/4 + info.inHeight%4);
				
				InputStream input = new FileInputStream(info.inImagePath);
				ImageInputStream stream = ImageIO.createImageInputStream(input); // File or input stream
				Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
				
				if (readers.hasNext()) {
					ImageReader reader = readers.next();
					reader.setInput(stream);

					ImageReadParam param = reader.getDefaultReadParam();
					param.setSourceRegion(sourceRegion); // Seteaza regiunea de citit

					BufferedImage imageChunk = reader.read(0, param); // Va fi citita doar bucata specificata(parametrul param - zona Rectangle); chunk de 1/4 din imagine
					buffer.put(imageChunk);
					
					if(!"silent".equals(info.parameter.toLowerCase()) && !"silent".equals(info.parameter2.toLowerCase()))
						System.out.println(i + "/4 of image sent"); 
					
					if(i == 4){									//Calculez timpul imediat ce trimiterea e gata; nu iau in considerare ultimul sleep
						producerEndTime = System.nanoTime();
						
						long producerDuration = (producerEndTime - producerStartTime);
						System.out.println("Reading and sending image from producer duration: " + producerDuration/1000000 + " milliseconds");
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e){
						
					}
				}		
			}
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
}

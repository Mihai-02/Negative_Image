package process;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class WriterResult implements Runnable {
	private DataInputStream in;		//Pt primire biti imagine de la Consumer
	private ProcessInfo info;		//Pt path fisier de iesire
	Thread t;
	private Buffer signalBuffer;	//Pt semnal de start si dim chunk
	long writerStartTime;
	long writerEndTime;
	
	public WriterResult(DataInputStream in, ProcessInfo info, Buffer signalBuffer){
		this.in = in;
		this.info = info;
		this.signalBuffer = signalBuffer;
		t = new Thread(this, "WriterResult");
		waitConfirmation();
	}
	
	public void waitConfirmation(){
		signalBuffer.getInt();
		writerStartTime = System.nanoTime();	//Start thread WriterResult
		t.start();
	}
	
	public void run(){
		int heightTotal = info.inHeight;
		int heightCurr = 0;
        BufferedImage fullImage = new BufferedImage(info.inWidth, heightTotal, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = fullImage.createGraphics();	//obiect tip Graphics2D pt "desenarea" pixelilor in imaginea fullImage creata mai sus
        
        BufferedImage imageChunk = null;
        int imageBytesLength = 0;
        //if(i == 1)	//Preiau dimensiunea unui sfert de imagine doar o data
			imageBytesLength = signalBuffer.getInt();
		for(int i=1; i<=4; i++){
			
			
			byte[] imageBytes = new byte[imageBytesLength];
				
			try {
				for(int j = 0; j < imageBytes.length/1024; j++)	//Citesc cu aceleasi constrangeri ca la trimitere in Consumer
					in.read(imageBytes, 1024 * j, 1024);
				in.read(imageBytes, imageBytes.length/1024*1024,imageBytes.length-imageBytes.length/1024*1024);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Pun bitii cititi intr-un ByteArrayInputStream pentru a-i citi in imageChunk cu ImageIO.read 
			ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
		    try {
				imageChunk = ImageIO.read(is);	
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
			if(!"silent".equals(info.parameter.toLowerCase()) && !"silent".equals(info.parameter2.toLowerCase()))
				System.out.println(i+"/4 of image received by WriterResult");
			
			
			//Optional: salvarea fiecarui chunk negative ca o imagine separata
			if("full".equals(info.parameter.toLowerCase()) || "full".equals(info.parameter2.toLowerCase())){
				try {
				    File outputfile = new File("PostTransformationChunk_" + i + ".bmp");
				    ImageIO.write(imageChunk, "bmp", outputfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		       
			//Desenare a cate unui chunk
		    g2d.drawImage(imageChunk, 0, heightCurr, null);
		    heightCurr += imageChunk.getHeight();
		}
		g2d.dispose();
		
		writerEndTime = System.nanoTime();
		long writerDuration = (writerEndTime - writerStartTime);
		System.out.println("Reading image in WriterResult duration: " + writerDuration/1000000 + " milliseconds");
		
		
		try {
		    File outputfile = new File(info.outImagePath);
		    ImageIO.write(fullImage, "bmp", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Process done!");
	}
}

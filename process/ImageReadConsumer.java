	package process;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;


import javax.imageio.ImageIO;

public class ImageReadConsumer implements Runnable{
	private DataOutputStream out;	//Pt comunicatie Consumer-WriterResult
	private Buffer imageBuffer;		//Pt primire imagine din consumer
	private Buffer signalBuffer;	//Pt primire semnal incepere      (2 buffere diferite pt claritate)
	private Thread t;
	private ProcessInfo info;		//Dimensiuni imagine intrare si path fisier iesire
	long consumerStartTime;
	long consumerEndTime;
	
	public ImageReadConsumer (Buffer b, ProcessInfo info, DataOutputStream out, Buffer signalBuffer) {
		consumerStartTime = System.nanoTime();		//Start Thread Consumer
		imageBuffer = b;
		this.signalBuffer = signalBuffer;
		this.out = out;
		this.info = info;
		t = new Thread(this, "Consumer");
		t.start();
	}
	
	public void run() {
		BufferedImage imageChunk;
		int heightTotal = info.inHeight;
		int heightCurr = 0;
		BufferedImage fullImage = new BufferedImage(info.inWidth, heightTotal, BufferedImage.TYPE_INT_RGB);	//Creez imaginea in care pun cele 4 chunks in ordine
        Graphics2D g2d = fullImage.createGraphics();	//Pentru a "desena" chunks in imagine
		
		for(int i=1; i <= 4; i++){
			imageChunk = imageBuffer.get();			//Citesc cate un chunk
	        g2d.drawImage(imageChunk, 0, heightCurr, null);	//Si umplu sfertul respectiv
	        heightCurr += imageChunk.getHeight();	//Actualizez pozitia de start a urmatorului chunk(pe inaltime)
	        
	        if(!("silent".equals(info.parameter.toLowerCase())) && !("silent".equals(info.parameter2.toLowerCase())))
				System.out.println(i+"/4 of image received");
	        
	        //Optional: salvarea fiecarui chunk ca o imagine separata
			if("full".equals(info.parameter.toLowerCase()) || "full".equals(info.parameter2.toLowerCase())){
				try {
				    File outputfile = new File("PreTransformationChunk_" + i + ".bmp");
				    ImageIO.write(imageChunk, "bmp", outputfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	
		g2d.dispose();
		
		consumerEndTime = System.nanoTime();	//Consumer termina preluarea imaginii din buffer
		long consumerDuration = (consumerEndTime - consumerStartTime);
		System.out.println("Receiving image in Consumer duration: " + consumerDuration/1000000 + " milliseconds");
		
		
		if(!"silent".equals(info.parameter.toLowerCase()) && !"silent".equals(info.parameter2.toLowerCase())){
			System.out.println("Image received!");
			System.out.println("Starting transformation...");
		}
		
		//Transformarea negative propriu-zisa
		long negativeStartTime = System.nanoTime();		//Inceput transformare

		for (int y = 0; y < info.inHeight; y++) {
	        for (int x = 0; x < info.inWidth; x++) {
	            int p = fullImage.getRGB(x, y);
	            int r = (p >> 16) & 0xff;	//Extrag cei 8 biti corespunzatori fiecarei culori din fiecare pixel
	            int g = (p >> 8) & 0xff;
	            int b = p & 0xff;
	
	            r = 255 - r;	//Inversez valoarea fata de cea maxima
	            g = 255 - g;
	            b = 255 - b;
	
	            p = (r << 16) | (g << 8) | b;	
	            fullImage.setRGB(x, y, p);	//Adaug in imagine pixelii noi
	        }
	    }
		if(!("silent".equals(info.parameter.toLowerCase())) && !("silent".equals(info.parameter2.toLowerCase())))
		System.out.println("Transformation done!");
		
		long negativeEndTime = System.nanoTime();		//Sfarsit transformare
		long negativeDuration = (negativeEndTime - negativeStartTime);
		System.out.println("Negative transform duration: " + negativeDuration/1000000 + " milliseconds");
		
		
		//Start trimitere a imaginii catre WriterResult
		consumerStartTime = System.nanoTime();
		
		signalBuffer.put(1);		//Trimite un semnal pentru declansarea lui WriterResult
		sendToWriter(fullImage);
		
		
		consumerEndTime = System.nanoTime();	//Final trimitere imagine
		
		consumerDuration = (consumerEndTime - consumerStartTime);
		System.out.println("Sending image from consumer to WriterResult duration: " + consumerDuration/1000000 + " milliseconds");
	}
	
	void sendToWriter(BufferedImage fullImage){		//Trimite imaginea fullImage catre WriteResult
		BufferedImage imageChunk;
		for(int i=1; i<=4; i++){
			//Fiecare sfert de imagine preluat cu getSubImage e transformat in byte[] pt a putea fi trimis prin pipe
			imageChunk = fullImage.getSubimage(0, ((i-1) * info.inHeight)/4, (int)info.inWidth, (int)info.inHeight/4);
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try {
				ImageIO.write(imageChunk, "bmp", byteArray);
			} catch (IOException e) {
				e.printStackTrace();
			}
			byte[] imageBytes = byteArray.toByteArray();
			
			if(i == 1)								//E nevoie de trimiterea dimensiunii unui sfert(in biti) doar o singura data
				signalBuffer.put(imageBytes.length);
			
			        
			try {
				for(int j = 0; j < imageBytes.length/1024; j++)	//Trimit bitii in segmente de 1024(maxim permis de pipe la un moment dat)
					out.write(imageBytes, j*1024, 1024);
				//in cazul in care dimensiunea nu e divizibila cu 1024
				out.write(imageBytes, imageBytes.length/1024*1024,imageBytes.length-imageBytes.length/1024*1024);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!"silent".equals(info.parameter.toLowerCase()) && !"silent".equals(info.parameter2.toLowerCase()))
				System.out.println(i + "/4 of image sent by consumer"); 
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e){
				e.printStackTrace();
			}
		}		
	}
}

package process;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class NegativeImageProcess extends ImageProcess{
	static int inWidth, inHeight;	//Dimensiunile imaginii de intrare
	static String parameter;
	static String parameter2;
	//Pipe-urile folosite in comunicarea Consumer-WriterResult
	PipedOutputStream pipeOut;
	PipedInputStream pipeIn;
	DataOutputStream out;
	DataInputStream in;
	
	public NegativeImageProcess(String[] args){
		super(args);
		getInputImageDimensions(inImagePath);
		
		if(args.length >= 3)
			parameter = args[2];
		if(args.length >= 4)
			parameter2 = args[3];
	}
	//bloc static de initializare
	static {
		System.out.println("Initialising Negative Image Process...");
		inWidth = 0;
		inHeight = 0;
		parameter = "default";
		parameter2 = "default";
	}
	
	public void getInputImageDimensions(String inImagePath){
		try{
			//Preluarea dimensiunilor imaginii se face fara citire propriu zisa a continutului
			InputStream input = new FileInputStream(inImagePath);
			ImageInputStream stream = ImageIO.createImageInputStream(input);	//Introduc imagine intr-un ImageInputStream prin path-ul imaginii
		    Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);	//Cu iteratorul accesez elementul Stream-ului (imaginea)
		    if (readers.hasNext()) {
		        ImageReader reader = readers.next();	//Stochez in obiect ImageReader care permite preluarea directa a dimensiunilor fara citirea continutului
		        try {
		            reader.setInput(stream);
		            inWidth = reader.getWidth(0);
		            inHeight = reader.getHeight(0);
		        } finally {
		            reader.dispose();
		        }
		}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runProcess(){
		//Pipe-urile pentru comunicatia Consumer-WriterResult
		pipeOut = new PipedOutputStream();
		try {
			pipeIn = new PipedInputStream(pipeOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		out = new DataOutputStream(pipeOut);
		in = new DataInputStream(pipeIn);
		
		Buffer b = new Buffer();				//Folosit pentru transmiterea de chunks intre Producer si Consumer
		Buffer signalBuffer = new Buffer();		//Folosit pentru transmiterea semnalului de incepere si a dimensiunii unui chunk catre WriterResult
		
		
		ProcessInfo info = null;	//informatia ce va fi predata thread-urilor
		//Initializarea ProcessInfo se face in functie de nr de parametri oferiti in linie de comanda
		if("default".equals(parameter))
			try {
				info = new ProcessInfo(inWidth, inHeight, inImagePath, outImagePath);
			} catch (InvalidPathException e1) {
				e1.printStackTrace();
			}
		else if("default".equals(parameter2))
			try {
				info = new ProcessInfo(inWidth, inHeight, inImagePath, outImagePath, parameter);
			} catch (InvalidPathException e1) {
				e1.printStackTrace();
			}
		else
			try {
				info = new ProcessInfo(inWidth, inHeight, inImagePath, outImagePath, parameter, parameter2);
			} catch (InvalidPathException e1) {
				e1.printStackTrace();
			}
		
		//Initializare Thread-uri
		ImageReadProducer producer = new ImageReadProducer(b, info);
		ImageReadConsumer consumer = new ImageReadConsumer(b, info, out, signalBuffer);
		WriterResult result = new WriterResult(in, info, signalBuffer);

		
		//Se verifica finalizarea thread-ului final, WriterResult pentru calcularea ulterioara
		//a timpului de executie a intregului proces
		try {
			result.t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		endTime = System.nanoTime();	//Finalizare proces; scriere timp total de executie in fisier si pe ecran
		double duration = (endTime - startTime);
		System.out.println("Full process duration: " + (long)duration/1000000 + " milliseconds");
		try {
			executionTimes.write("Full process duration: " + duration/1000000000 + " seconds\n");
			executionTimes.write("					   " + duration/1000000 + " milliseconds\n");
			executionTimes.write("					   " + duration + " nanoseconds");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			executionTimes.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}

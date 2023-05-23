package process;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public abstract class ImageProcess implements FileProcess{
	String[] args;						//Argumentele din linia de comanda
	String inImagePath, outImagePath;	//Path urile extrase din args
	long startTime;					//Momente de timp stocate pt calcularea timpului de executie
	long endTime;
	FileWriter executionTimes;	//Fisierul de scriere a timpului
	
	public ImageProcess(String[] args) {
		startTime = System.nanoTime();		//Inceperea intregului proces de prelucrare a imaginii
		try {
			executionTimes = new FileWriter(timeLogFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.args = args;
		getFilesPath();
	}
	
	public void getFilesPath(){	//Se preiau path urile fisierelor
		Scanner scan= new Scanner(System.in);		
		if(args.length == 0){		//Nu a fost introdusa nicio cale de fisier ca argument in linie de comanda => se citesc de la tastatura
			System.out.println("Introduceti numele fisierului de intrare:");
			inImagePath = scan.nextLine();
			System.out.println("Introduceti numele fisierului de iesire:");
			outImagePath = scan.nextLine();
		}
		else if(args.length == 1){	//A fost introdusa doar calea fisierului de intrare ca argument in linie de comanda
			inImagePath = args[0];
			System.out.println("Introduceti numele fisierului de iesire:");//Se citeste doar al doilea fisier de la tastatura
			outImagePath = scan.nextLine();
		}
		else{	//Au fost introduse ambele cai
			inImagePath = args[0];
			outImagePath = args[1];
		}
		scan.close();
	}
	
	public abstract void runProcess();	//Functie ce este implementata in functie de tipul de proces
	
}

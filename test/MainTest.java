/*==============================================
 * 			Ciocan Constantin-Mihai
 =============================================*/
package test;

import process.NegativeImageProcess;

public class MainTest {

	public static void main(String[] args) {
		
		//ordinea argumentelor in linie de comanda:
		//<locatie_fisier_intrare> <locatie_creare_fisier_iesire> <parametru_1(silent/full)> <parametru_2(full/silent)>
		
		//FUNCTIE DE HELP
		//AFISEZ PE ECRAN SINTAXA args
		if(args.length == 1 && "help".equals(args[0].toLowerCase())){
			System.out.println("Syntax:  <input_image_path> <output_image_path> <parameters(silent/full)>");
		}
		else{
			NegativeImageProcess process = new NegativeImageProcess(args);	//creez
			process.runProcess();	//si rulez procesul
		}
	}
}

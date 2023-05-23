package process;

//Clasa ProcessInfo are rolul de a oferi thread-urilor informatiile despre imagini(paths, dimensiuni)
public class ProcessInfo {
	String inImagePath, outImagePath;
	int inWidth, inHeight;
	String parameter;		//Parametri oferiti in linia de comanda sau "default"
	String parameter2;
	
	//bloc de initializare; in cazul in care nu sunt oferiti parametri acestia raman "default"
	{
		parameter = "default";
		parameter2 = "default";
	}
	
	public ProcessInfo(int inWidth, int inHeight, String ...arguments) throws InvalidPathException{
		this.inWidth = inWidth;
		this.inHeight = inHeight;
		
		//Verificare validitate nume fisiere
		if(arguments.length < 2 || (arguments[0].indexOf(".bmp") != arguments[0].length()-4 || 
				arguments[1].indexOf(".bmp") != arguments[1].length()-4))	
			throw new InvalidPathException("Invalid input/output image paths!");	//Daca nu, se arunca o exceptie custom
		else{							//Daca sunt valide sunt stocate path urile fisierelor
			inImagePath = arguments[0];
			outImagePath = arguments[1];
		}
		if(arguments.length >= 3)		//Exista cel putin un parametru
			parameter = arguments[2];
		if(arguments.length >= 4)		//Exista doi parametri
			parameter2 = arguments[3];
	}
}

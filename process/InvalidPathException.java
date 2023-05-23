package process;

//Exceptie custom care este utilizata atunci cand fisierele date ca argumente nu sunt format .bmp
public class InvalidPathException extends Exception {
	public InvalidPathException(String mesaj) {
		super(mesaj);
	}
}
package telran.view;
import java.io.*;
import java.util.Scanner;
public class StandardInputOutput implements InputOutput {
Scanner scanner = new Scanner(System.in);

	@Override
	public String readString(String prompt) {
		writeLine(prompt);
		return scanner.nextLine();
	}

	@Override
	public void writeString(Object obj) {
		System.out.println(obj.toString());
	}
}
package telran.git.controller;

import telran.git.*;
import telran.view.*;

public class GitRepositoryAppl {

	public static void main(String[] args) {
		InputOutput io = new StandardInputOutput();
		try {
			Menu menu = new Menu("Git Console Commands Menu",  GitItems.getGitItems(GitRepositoryImpl.init()));
			menu.perform(io);
		} catch (Exception e) {
			io.writeLine("Error: " + e.getMessage());
		}

	}
}

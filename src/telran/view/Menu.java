package telran.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Menu implements Item {
	private static final int STARS_AMOUNT = 20;
	private String name;
	private ArrayList<Item> items;

	public Menu(String name, ArrayList<Item> items) {
		this.name = name;
		this.items = items;
	}

	public Menu(String name, Item... items) {
		this(name, new ArrayList<>(Arrays.asList(items)));
	}

	@Override
	public String displayName() {
		return name;
	}

	@Override
	public void perform(InputOutput io) {
		boolean running = true;
		try {
			while(running) {
				displayTitle(io);
				displayItems(io);
				int itemNumber = io.readInt("Enter item number", "Wrong item number", 1, items.size());
				Item item = items.get(itemNumber - 1);
				item.perform(io);
				if(item.isExit()) {
					running = false;
				}
			}
		} catch (Exception e) {
			io.writeLine(e.getMessage());
		}
		io.writeLine("Thanks & Goodbye");
	}

	private void displayItems(InputOutput io) {
		IntStream.rangeClosed(1, items.size()).forEach(i ->
		io.writeLine(String.format("%d. %s", i, items.get(i - 1).displayName())));
		
	}

	private void displayTitle(InputOutput io) {
		io.writeLine("*".repeat(STARS_AMOUNT));
		io.writeLine(String.format("*%s%s", " ".repeat(STARS_AMOUNT / 4), name));
		io.writeLine("*".repeat(STARS_AMOUNT));
		
	}

	@Override
	public boolean isExit() {
		return false;
	}
}
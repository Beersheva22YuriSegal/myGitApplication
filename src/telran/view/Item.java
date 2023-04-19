package telran.view;

import java.util.function.Consumer;

public interface Item {
	String displayName();

	void perform(InputOutput io);

	boolean isExit();

	static Item of(String name, Consumer<InputOutput> consumer, boolean exit) {
		return new Item() {

			@Override
			public String displayName() {
				return name;
			}

			@Override
			public void perform(InputOutput io) {
				consumer.accept(io);
			}

			@Override
			public boolean isExit() {
				return exit;
			}
		};
	}

	static Item of(String name, Consumer<InputOutput> consumer) {
		return of(name, consumer, false);
	}

	static Item exit() {
		return of("Exit", io -> {
		}, true);
	}
}
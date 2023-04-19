package telran.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.function.*;

public interface InputOutput {
	String readString(String prompt);

	void writeString(Object obj);

	default void writeLine(Object obj) {
		writeString(obj.toString() + "\n");
	}

	default <R> R readObject(String prompt, String errorPrompt, Function<String, R> mapper) {
		boolean running = true;
		R result = null;
		while (running) {
			try {
				String str = readString(prompt);
				result = mapper.apply(str);
				running = false;
			} catch (Exception e) {
				writeLine(errorPrompt + " - " + e.getMessage());
			}
		}
		return result;
	}

	default String readStringPredicate(String prompt, String errorPrompt, Predicate<String> predicate) {
		return readObject(prompt, errorPrompt, s -> {
			if (!predicate.test(s)) {
				throw new RuntimeException();
			}
			return s;
		});
	}

	default String readStringOptions(String prompt, String errorPrompt, Set<String> options) {
		return readStringPredicate(prompt, errorPrompt, options::contains);
	}

	default int readInt(String prompt, String errorPrompt) {
		return readObject(prompt, errorPrompt, Integer::parseInt);
	}
	
	default int readInt(String prompt, String errorPrompt, int min, int max) {
		return readObject(prompt, errorPrompt, s -> {
			try {
				int num = Integer.parseInt(s);
				checkRange(min, max, num);
				return num;
			} catch (NumberFormatException e) {
				throw new RuntimeException("must be a number");
			}
		});
	}

	default long readLong(String prompt, String errorPrompt, long min, long max) {
		return readObject(prompt, errorPrompt, s -> {
			try {
				long num = Long.parseLong(s);
				checkRange(min, max, num);
				return num;
			} catch (NumberFormatException e) {
				throw new RuntimeException("must be a number");
			}
		});
	}

	default double readNumber(String prompt, String errorPrompt, double min, double max) {
		return readObject(prompt, errorPrompt, s -> {
			try {
				double num = Double.parseDouble(s);
				checkRange(min, max, num);
				return num;
			} catch (NumberFormatException e) {
				throw new RuntimeException("must be a number");
			}
		});
	}

	default void checkRange(double min, double max, double num) {
		if (num < min) {
			throw new RuntimeException("Number should be greater than " + min);
		}
		if (num > max) {
			throw new RuntimeException("Number should be lesser than " + max);
		}
	}
	
	default LocalDate readDateISO(String prompt, String errorPrompt) {
		return readObject(prompt, errorPrompt, LocalDate::parse);
	}

	default LocalDate readDate(String prompt, String errorPrompt, String format, LocalDate min, LocalDate max) {
		return readObject(prompt, errorPrompt, s -> {
			DateTimeFormatter dtf = null;
			try {
				dtf = DateTimeFormatter.ofPattern(format);
			} catch (Exception e) {
				throw new RuntimeException("Wrong date format " + format);
			}
			LocalDate res = null;
			try {
				res = LocalDate.parse(s, dtf);
			} catch (Exception e) {
				throw new RuntimeException("must be date in format " + format);
			}
			if (res.isBefore(min)) {
				throw new RuntimeException("Date should not be before " + min.format(dtf));
			}
			if (res.isAfter(max)) {
				throw new RuntimeException("Date should not be after " + max.format(dtf));
			}
			return res;
		});
	}
}
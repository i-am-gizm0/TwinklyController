import org.apache.commons.cli.ParseException;

public class Main {

	public static void main(String[] args) throws ParseException {
		if (args.length > 0) {
			try {
				CLI.main(args);
			} catch (Exception e) {
				System.err.println("Something went wrong parsing the arguments passed: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		} else {
			GUI.main(args);
		}
	}

}

import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CLI {
	private static HelpFormatter formatter;
	private static Options options;

	public static void main(String[] args) throws Exception {
		String ip;
		TwinklyController controller;
		
		options = new Options();
		Option address = Option.builder()
				.longOpt("ip")
				.argName("ip")
				.hasArg()
				.required()
				.desc("Twinkly IP Address")
				.build();
		options.addOption(address);
		Option action = Option.builder()
				.longOpt("action")
				.argName("action")
				.hasArg()
				.required()
				.desc("Requested Action")
				.build();
		options.addOption(action);
		options.addOption("write", "Specifies writing data. --movie implies --write");
		Option file = Option.builder()
				.longOpt("movie")
				.argName("file name")
				.hasArg()
				.desc("Movie image file. Ignored unless using `--action movie`")
				.build();
		options.addOption(file);
		Option fps = Option.builder()
				.longOpt("fps")
				.argName("fps")
				.hasArg()
				.desc("FPS to run movie at. Ignored unless using `--action movie` and `--movie ...` is present")
				.build();
		options.addOption(fps);
		
		formatter = new HelpFormatter();
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printHelp();
			return;
		}
		if (cmd.hasOption("ip")) {	// Everything requires an IP so if it's not there, don't try anything else
			ip = cmd.getOptionValue("ip");
			if (Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
					.matcher(ip).find()) {	// Is it in the form of an IP Address?
				// TODO: Check for a valid IP
				System.out.println("The IP Address you want to control is " + ip);
			}
			controller = new TwinklyController(ip);
			if (cmd.hasOption("action")) {	// Does it have an action?
				boolean set = cmd.hasOption("write");
				
				switch (cmd.getOptionValue("action")) {
					case "gestalt":
						System.out.println(controller.gestalt());
						break;
						
					case "name":
						if (!set) {	// Read
							System.out.println(controller.getName());
						} else {
							// TODO: Write
						}
						break;
						
					case "timer":
						if (!set) { // Read
							Timer t = controller.getTimer();
							System.out.println("NOW " + t.getNow());
							System.out.println("OFF " + t.getOff());
							System.out.println("ON  " + t.getOn());
						} else {
							// TODO: Write
						}
						break;
						
					case "movie":
						if (!(cmd.hasOption("movie") && cmd.hasOption("fps"))) { // Just set mode. 
							controller.setMode(TwinklyController.Mode.MOVIE);
						} else {
							
						}
				}
			}
		} else {
			printHelp();
		}
	}
	
	public static void printHelp() {
		String executableName = (new java.io.File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())).toString();
		executableName = "java -jar Twinkly.jar";
		formatter.printHelp(executableName, options);
	}

}

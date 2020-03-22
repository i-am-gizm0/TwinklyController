package net.gizm0.twinkly;
import java.io.File;
import java.io.IOException;

import com.google.common.net.InetAddresses;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.gizm0.exception.ArgumentException;
import net.gizm0.exception.MalformedResponseException;
import net.gizm0.exception.NetworkException;
import net.gizm0.exception.ServerRejectException;

public class CLI {
	private static HelpFormatter formatter;
	private static Options options;

	/**
	 * The method called when the program is called with arguments on the command line
	 * @param args The arguments from the command line
	 * @throws IOException When a general I/O error occurs
	 * @throws NetworkException When a network problem occurs
	 * @throws MalformedResponseException When the server's HTTP response is malformed
	 * @throws org.json.simple.parser.ParseException When the server's JSON response is malformed
	 * @throws ServerRejectException When the server rejects a request
	 * @throws ArgumentException When the CLI arguments are invalid
	 */
	public static void main(String[] args) throws IOException, NetworkException, MalformedResponseException, org.json.simple.parser.ParseException, ServerRejectException,
			ArgumentException {
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
		Option fpsArg = Option.builder()
				.longOpt("fps")
				.argName("fps")
				.hasArg()
				.desc("FPS to run movie at. Ignored unless using `--action movie` and `--movie ...` is present")
				.build();
		options.addOption(fpsArg);
		
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
			if (InetAddresses.isInetAddress(ip)) {
			// if (Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
			// 		.matcher(ip).find()) {	// Is it in the form of an IP Address?
				System.out.println("The IP Address you want to control is " + ip);
			} else {
				throw new ArgumentException("The IP Address is not valid");
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
							File image;
							int fps;
							if ((fps = Integer.parseInt(cmd.getOptionValue("fps"))) <= 30) {
								try {
									image = new File(cmd.getOptionValue("movie"));
									controller.uploadMovie(image, fps);
								} catch (NullPointerException e) {
									throw new IOException("x001 FailFOpen Could not open file");
								}
							}
						}
						break;
						
					case "demo":
						controller.setMode(TwinklyController.Mode.DEMO);
						break;
						
					case "off":
						controller.setMode(TwinklyController.Mode.OFF);
						break;
						
					case "fwver":
						System.out.println(controller.getFirmwareVersion());
				}
			}
		} else {
			printHelp();
		}
	}
	
	/**
	 * Prints the help message to the terminal
	 */
	public static void printHelp() {
		String executableName = (new java.io.File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())).toString();
		executableName = "java -jar Twinkly.jar";
		formatter.printHelp(executableName, options);
	}

}

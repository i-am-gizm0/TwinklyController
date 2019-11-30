import java.io.File;
import java.net.MalformedURLException;

public class Tester {

	public static void main(String[] args) throws Exception {
		args = "--ip 192.168.1.250 --action gestalt --write".split(" ");
		CLI.main(args);
	}

}

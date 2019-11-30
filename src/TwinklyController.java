import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TwinklyController {
	
	private String IP;
	
	private URL mainURL;
	private URL loginURL;
	private URL verifyURL;
	
	HttpURLConnection connection;
	
	private String authToken;
	private String challengeResponse;
	
	private JSONParser parser;

	private RandomString challenge = new RandomString(32);
	
	public TwinklyController(String IP) throws Exception {
		this.IP = IP;
		loginURL = new URL("http://" + IP + "/xled/v1/login");
		verifyURL = new URL("http://" + IP + "/xled/v1/verify");
		parser = new JSONParser();
		gestalt();
	}

	private void setupConnection(URL url, String requestMethod) throws IOException {
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(requestMethod);
		connection.addRequestProperty("X-Auth-Token", authToken);
		connection.setDoOutput(true);
	}

	private String readFromConnection(HttpURLConnection con) throws UnsupportedEncodingException, IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null) { // While there are more lines to read from the response...
			sb.append(line + "\n");
		}
		br.close();
		return sb.toString();
	}
	
	private void login() throws Exception {
		// Create request and send it
		HttpURLConnection con = (HttpURLConnection) loginURL.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		
		OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
		String requestBody = "{\"challenge\": \"" + challenge.nextString() + "\"}";
		wr.write(requestBody);
		wr.flush();
		
		// Receive response
		int result = con.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(con);
			
			// We have the response! Time to parse it
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			
			// It parsed successfully! Time to get the token
			if ((authToken = (String) parsedJSON.get("authentication_token")) == null) {
				// The response didn't contain an authentication token! Time to die!
				throw new Exception(
						((Long) parsedJSON.get("code") != null ?
								((Long) parsedJSON.get("code")).toString()
							: "0000"
						) + " AuthToken Response:" + response);
			}
			
			if ((challengeResponse = (String) parsedJSON.get("challenge-response")) == null) {
				// The response didn't contain a challenge response! Time to die!
				throw new Exception(
						((Long) parsedJSON.get("code") != null ?
								((Long) parsedJSON.get("code")).toString()
							: "0000"
						) + " AuthToken Response:" + response);
			}
			
			// We got both an auth token and a challenge response! We're (almost) in! We have to verify first:
			verify();
		}
		// We're in! Hooray!
	}

	private void verify() throws Exception {
		
		setupConnection(verifyURL, "POST");
		
		OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
		String requestBody = "{\"challenge-response\": \"" + challengeResponse + "\"}";
		wr.write(requestBody);
		wr.flush();
		
		
		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			
			// We have the response! Time to parse it
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			if (parsedJSON.get("code") == null || (long) parsedJSON.get("code") != 1000) {
				throw new Exception(
						((Long) parsedJSON.get("code") != null ?
								((Long) parsedJSON.get("code")).toString()
							: "0000"
						) + " FailVerif Response:" + response);
			}
		}
		// We're in! Hooray!
	}
	
	public Twinkly gestalt() throws Exception {
		login();
		
		mainURL = new URL("http://" + IP + "/xled/v1/gestalt");
		setupConnection(mainURL, "GET");
		
		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			try {
				return new Twinkly((JSONObject) parser.parse(response));
			} catch (ParseException e) {
				throw new Exception("0" + Integer.toString(result) + " Bad_JSON Response: " + response);
			}
		} else {
			throw new Exception("0" + Integer.toString(result) + " Bad_HTTP Response: " + readFromConnection(connection));
		}
	}

	public String getName() throws Exception {
		login();
		
		mainURL = new URL("http://" + IP + "/xled/v1/device_name");
		setupConnection(mainURL, "GET");
		
		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			return (String) parsedJSON.get((Object) "name");
		} else {
			throw new Exception("0" + Integer.toString(result) + " Bad_HTTP Response: " + readFromConnection(connection));
		}
	}
	
	public void setName(String name) throws Exception {
		// Make sure the new name can't possibly break JSON
		if (name.length() >= 32) {
			throw new Exception("0001 Lng_Name NmLength: " + name.length());
		}
		for (int i = 0; i < name.length(); i++) {
			switch (name.charAt(i)) {
				case '\b':
				case '\f':
				case '\n':
				case '\r':
				case '\t':
				case '\"':
				case '\\':
					throw new Exception("0001 Bad_Name Position: " + i);
			}
		}
		
		login();
		
		mainURL = new URL("http://" + IP + "/xled/v1/device_name");
		setupConnection(mainURL, "POST");
		
		OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
		String requestBody = "{\"name\": \"" + name + "\"}";
		wr.write(requestBody);
		wr.flush();

		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			
			// We have the response! Time to parse it
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			if (parsedJSON.get("code") == null || (long) parsedJSON.get("code") != 1000) {
				throw new Exception(
						((Long) parsedJSON.get("code") != null ?
								((Long) parsedJSON.get("code")).toString()
							: "0000"
						) + " NmeReject Response:" + response);
			}
		}
	}
	
//	public void setAPMode() {
//		// TODO: Implement AP Mode
//	}
//	
//	public void setStationMode(String ssid, String password) {
//		// TODO: Implement Station mode
//	}
	
	public Timer getTimer() throws Exception {
		login();
		
		mainURL = new URL("http://" + IP + "/xled/v1/timer");
		setupConnection(mainURL, "GET");
		
		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			return new Timer(((Long)parsedJSON.get("time_now")).intValue(), ((Long)parsedJSON.get("time_on")).intValue(), ((Long)parsedJSON.get("time_off")).intValue());
		} else {
			throw new Exception("0" + Integer.toString(result) + " Bad_HTTP Response: " + readFromConnection(connection));
		}
	}

	public void setTimer(Timer timer) throws Exception {
		setTimer(timer, true);
	}

	public void setTimer(Timer timer, boolean useStrict) throws Exception {
		int now, on, off;
		now = timer.getNow();
		on = timer.getOn();
		off = timer.getOff();
		
		login();
		
		mainURL = new URL("http://" + IP + "/xled/v1/timer");
		setupConnection(mainURL, "POST");
		
		if (useStrict) {
			Calendar nowc = Calendar.getInstance();
			Calendar midnight = Calendar.getInstance();
			midnight.set(Calendar.HOUR_OF_DAY, 0);
			midnight.set(Calendar.MINUTE, 0);
			midnight.set(Calendar.SECOND, 0);
			midnight.set(Calendar.MILLISECOND, 0);
			now = (int) ((nowc.getTimeInMillis() - midnight.getTimeInMillis()) / 1000.0);
		}
		OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
		String requestBody = "{\"time_now\": " + now + ", \"time_on\": " + on + ", \"time_off\": " + off + "}";
		wr.write(requestBody);
		wr.flush();
		
		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			
			// We have the response! Time to parse it
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			if (parsedJSON.get("code") == null || (long) parsedJSON.get("code") != 1000) {
				throw new Exception(
						((Long) parsedJSON.get("code") != null ?
								((Long) parsedJSON.get("code")).toString()
							: "0000"
						) + " TmeReject Response:" + response);
			}
		}
	}
	
	/**
	 * Sets mode of Twinkly
	 * @param mode Mode to set (OFF, DEMO, MOVIE)
	 * @throws Exception if anything goes wrong
	 */
	
	public void setMode(Mode mode) throws Exception {
		int option = 0;
		switch (mode) {
			case DEMO:
				option = 1;
				break;
				
			case MOVIE:
				option = 2;
		}
		setMode(option);
	}

	/**
	 * Three options are valid:
	 * 0: off
	 * 1: demo (predefined set of effects that change every so often
	 * 2: movie (last uploaded effect)
	 * real time is not supported yet
	 * an invalid choice will revert to off
	 * @throws Exception 
	 */
	public void setMode(int option) throws Exception {
		if (option < 0 && option > 2) {
			option = 0;
		}
		String mode;
		switch (option) {
			case 1:
				mode = "demo";
				break;
				
			case 2:
				mode = "movie";
				break;
		
			case 0:
			default:
				mode = "off";
		}
		System.out.println("Mode: " + mode);
		
		login();
		
		mainURL = new URL("http://" + IP + "/xled/v1/led/mode");
		setupConnection(mainURL, "POST");
		
		OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
		String requestBody = "{\"mode\": \"" + mode + "\"}";
		wr.write(requestBody);
		wr.flush();

		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			
			// We have the response! Time to parse it
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			if (parsedJSON.get("code") == null || (long) parsedJSON.get("code") != 1000) {
				if ((long)parsedJSON.get("code") == 1104 && option == 2) {
					throw new Exception(
							((Long) parsedJSON.get("code") != null ?
									((Long) parsedJSON.get("code")).toString()
								: "0000"
							) + " BadMovie Response:" + response);
				}
				throw new Exception(
						((Long) parsedJSON.get("code") != null ?
								((Long) parsedJSON.get("code")).toString()
							: "0000"
						) + " MdeReject Response:" + response);
			}
		}
	}

	public String getFirmwareVersion() throws Exception {
		login();
		
		mainURL = new URL("http://" + IP + "/xled/v1/fw/version");
		setupConnection(mainURL, "GET");
		
		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			return (String) parsedJSON.get((Object) "version");
		} else {
			throw new Exception("0" + Integer.toString(result) + " Bad_HTTP Response: " + readFromConnection(connection));
		}
	}
	
	public void reset() throws Exception {
		login();
		
		mainURL = new URL("http://" + IP + "/xled/v1/led/reset");
		setupConnection(mainURL, "GET");
		connection.getResponseCode();
	}
	
	public void uploadMovie(File image, int fps) throws Exception {
		login();
		Twinkly lights = gestalt();
		int length = ((Long)lights.getNumberOfLED()).intValue();
		int maxframes = ((Long)lights.getMovieCapacity()).intValue();
		ImageProcessor processor = new ImageProcessor(image, length, maxframes);
		int frames = processor.getHeight();
		
//		try {
//			setMode(2);
//		} catch (Exception e) {
//			System.out.println("setMode failed. No problem. Continuing...");
//		}
		
		reset();
		
		mainURL = new URL("http://" + IP + "/xled/v1/led/movie/full");
		setupConnection(mainURL, "POST");
		connection.addRequestProperty("Content-Type", "application/octet-stream");
		
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		for (int y = 0; y < frames; y++) {
			for (int x = 0; x < length; x++) {
				Color c = processor.getColor(x, y);
				wr.write(c.getRed());
				wr.write(c.getGreen());
				wr.write(c.getBlue());
				System.out.println("Color at (" + x + ", " + y + ") is rgb(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")");
			}
		}
		wr.flush();
		
		boolean tryAgainLater = false;
		// Receive response
		int result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			System.out.println("MOVIE RESPONSE: " + response);
			
			// We have the response! Time to parse it
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			if (parsedJSON.get("code") == null || ((long) parsedJSON.get("code") != 1000 && (long) parsedJSON.get("code") != 1100)) {
				throw new Exception(
						((Long) parsedJSON.get("code") != null ?
								((Long) parsedJSON.get("code")).toString()
							: "0000"
						) + " MdeReject Response:" + response);
			}
			if (parsedJSON.get("frames_number") == null || ((Long)parsedJSON.get("frames_number")).intValue() != frames) {
				tryAgainLater = true;
			}
		}
		
		
		
		mainURL = new URL("http://" + IP + "/xled/v1/led/movie/config");
		setupConnection(mainURL, "POST");
		
		OutputStreamWriter wr1 = new OutputStreamWriter(connection.getOutputStream());
		String requestBody = "{\"frame_delay\": " + (int)(1000.0 / fps) + ", \"leds_number\": " + length + ", \"frames_number\": " + frames + "}";
		wr1.write(requestBody);
		wr1.flush();

		// Receive response
		result = connection.getResponseCode();
		if (result == HttpURLConnection.HTTP_OK) {
			// Twinkly responded with a 200 OK! All good so far
			String response = readFromConnection(connection);
			
			// We have the response! Time to parse it
			JSONObject parsedJSON = (JSONObject) parser.parse(response);
			if (parsedJSON.get("code") == null || (long) parsedJSON.get("code") != 1000) {
				throw new Exception(
						((Long) parsedJSON.get("code") != null ?
								((Long) parsedJSON.get("code")).toString()
							: "0000"
						) + " CfgReject Response:" + response);
			}
		}
		
		reset();
		
		try {
			setMode(2);
		} catch (Exception e) {
			System.out.println("setMode failed. I'm getting worried...");
		}
		
		if (tryAgainLater) {
			setupConnection(mainURL, "POST");
			connection.addRequestProperty("Content-Type", "application/octet-stream");
			
			wr = new DataOutputStream(connection.getOutputStream());
			for (int y = 0; y < frames; y++) {
				for (int x = 0; x < length; x++) {
					Color c = processor.getColor(x, y);
					wr.write(c.getRed());
					wr.write(c.getGreen());
					wr.write(c.getBlue());
				}
			}
			wr.flush();
			
			// Receive response
			result = connection.getResponseCode();
			if (result == HttpURLConnection.HTTP_OK) {
				// Twinkly responded with a 200 OK! All good so far
				String response = readFromConnection(connection);
				
				// We have the response! Time to parse it
				JSONObject parsedJSON = (JSONObject) parser.parse(response);
				if (parsedJSON.get("code") == null || ((long) parsedJSON.get("code") != 1000 && (long) parsedJSON.get("code") != 1100)) {
					throw new Exception(
							((Long) parsedJSON.get("code") != null ?
									((Long) parsedJSON.get("code")).toString()
								: "0000"
							) + " MdeReject Response:" + response);
				}
				if (parsedJSON.get("frames_number") == null || (int)parsedJSON.get("frames_number") != frames) {
					throw new Exception(
							((Long) parsedJSON.get("code") != null ?
									((Long) parsedJSON.get("code")).toString()
								: "0000"
							) + " MovReject Response:" + response);
				}
			}
			
			reset();
			
			try {
				setMode(2);
			} catch (Exception e) {
				System.out.println("setMode failed. Something has gone terribly wrong");
			}
		}
	}
	
	public enum Mode {
		OFF,
		DEMO,
		MOVIE
	}
}

package net.gizm0.twinkly;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import net.gizm0.exception.ArgumentException;
import net.gizm0.exception.MalformedResponseException;
import net.gizm0.exception.NetworkException;
import net.gizm0.exception.ServerRejectException;
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

    /**
     * Creates a new controller and verifies the connection
     *
     * @param IP The network address of the device
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     */
    public TwinklyController(String IP)
            throws IOException, NetworkException, MalformedResponseException, ParseException {
        this.IP = IP;
        loginURL = new URL("http://" + IP + "/xled/v1/login");
        verifyURL = new URL("http://" + IP + "/xled/v1/verify");
        parser = new JSONParser();
        gestalt();
    }

    /**
     * Sets up most connections: sets the request method and the auth token and allows us to read
     * the connection
     *
     * @param url the URL for the connection
     * @param requestMethod a string of the request method ("GET" or "POST")
     * @throws IOException if a generic I/O error occurs if the method is invalid, probably
     */
    private void setupConnection(URL url, String requestMethod) throws IOException {
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.addRequestProperty("X-Auth-Token", authToken);
        connection.setDoOutput(true);
    }

    /**
     * Returns the string of the server's response
     *
     * @param con the connection to read from
     * @return the server's response
     * @throws UnsupportedEncodingException if Java can't read the text encoding. This probably
     *     won't happen
     * @throws IOException if a generic I/O error occurs If the input stream is inaccessible
     */
    private String readFromConnection(HttpURLConnection con)
            throws UnsupportedEncodingException, IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br =
                new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
        String line = null;
        while ((line = br.readLine())
                != null) { // While there are more lines to read from the response...
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }

    /**
     * Initiate the authentication process
     *
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json if the server's response is
     *     malformed json
     */
    private void login()
            throws IOException, NetworkException, MalformedResponseException, ParseException {
        // Create request and send it
        HttpURLConnection con = (HttpURLConnection) loginURL.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        String requestBody =
                "{\"challenge\": \""
                        + challenge.nextString()
                        + "\"}"; // I could have created `JSONObject`s but this is easier
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
                throw new MalformedResponseException(
                        ((Long) parsedJSON.get("code") != null
                                        ? ((Long) parsedJSON.get("code")).toString()
                                        : "0000")
                                + " AuthToken Response:"
                                + response);
            }

            if ((challengeResponse = (String) parsedJSON.get("challenge-response")) == null) {
                // The response didn't contain a challenge response! Time to die!
                throw new MalformedResponseException(
                        ((Long) parsedJSON.get("code") != null
                                        ? ((Long) parsedJSON.get("code")).toString()
                                        : "0000")
                                + " AuthToken Response:"
                                + response);
            }

            // We got both an auth token and a challenge response! We're (almost) in! We have to
            // verify first:
            verify();
        } else {
            throw new NetworkException(
                    "0" + result + " BadHTTP Response: " + readFromConnection(con));
        }
        // We're in! Hooray!
    }

    /**
     * Complete the authentication process
     *
     * @throws IOException if a generic I/O error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     * @throws NetworkException if a generic network error occurs
     */
    private void verify()
            throws IOException, MalformedResponseException, ParseException, NetworkException {

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
                throw new MalformedResponseException(
                        ((Long) parsedJSON.get("code") != null
                                        ? ((Long) parsedJSON.get("code")).toString()
                                        : "0000")
                                + " FailVerif Response:"
                                + response);
            }
        } else {
            throw new NetworkException(
                    "0" + result + " BadHTTP Response: " + readFromConnection(connection));
        }
        // We're in! Hooray!
    }

    /**
     * Get the status information from the device
     *
     * @return a {@link Twinkly} object containing the returned values
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     */
    public Twinkly gestalt()
            throws IOException, NetworkException, MalformedResponseException, ParseException {
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
                throw new MalformedResponseException(
                        "0" + Integer.toString(result) + " Bad_JSON Response: " + response);
            }
        } else {
            throw new NetworkException(
                    "0"
                            + Integer.toString(result)
                            + " Bad_HTTP Response: "
                            + readFromConnection(connection));
        }
    }

    /**
     * Gets the name of the device
     *
     * @return the name of the device
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     */
    public String getName()
            throws IOException, NetworkException, MalformedResponseException, ParseException {
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
            throw new NetworkException(
                    "0"
                            + Integer.toString(result)
                            + " Bad_HTTP Response: "
                            + readFromConnection(connection));
        }
    }

    /**
     * Set the name of the device
     *
     * @param name The new name for the device. It must be less than 32 characters and not contain
     *     {@code '\b'}, {@code '\f'}, {@code '\n'}, {@code '\r'}, {@code '\t'}, {@code '\"'}, or
     *     {@code '\\'}
     * @throws ArgumentException if an argument is invalid
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     * @throws ServerRejectException if the server rejects the name
     */
    public void setName(String name)
            throws ArgumentException, IOException, NetworkException, MalformedResponseException,
                    ParseException, ServerRejectException {
        // Make sure the new name can't possibly break JSON
        if (name.length() >= 32) {
            throw new ArgumentException("0001 Lng_Name NmLength: " + name.length());
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
                    throw new ArgumentException("0001 Bad_Name Position: " + i);
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
                throw new ServerRejectException(
                        ((Long) parsedJSON.get("code") != null
                                        ? ((Long) parsedJSON.get("code")).toString()
                                        : "0000")
                                + " NmeReject Response:"
                                + response);
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

    /**
     * Gets the timer setting from the device
     *
     * @return a {@link Timer} object containing the timer data
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     */
    public Timer getTimer()
            throws IOException, NetworkException, MalformedResponseException, ParseException {
        login();

        mainURL = new URL("http://" + IP + "/xled/v1/timer");
        setupConnection(mainURL, "GET");

        // Receive response
        int result = connection.getResponseCode();
        if (result == HttpURLConnection.HTTP_OK) {
            // Twinkly responded with a 200 OK! All good so far
            String response = readFromConnection(connection);
            JSONObject parsedJSON = (JSONObject) parser.parse(response);
            return new Timer(
                    ((Long) parsedJSON.get("time_now")).intValue(),
                    ((Long) parsedJSON.get("time_on")).intValue(),
                    ((Long) parsedJSON.get("time_off")).intValue());
        } else {
            throw new NetworkException(
                    "0"
                            + Integer.toString(result)
                            + " Bad_HTTP Response: "
                            + readFromConnection(connection));
        }
    }

    /**
     * Set the timer, overriding the current time for the correct time
     *
     * @param timer the timer to send
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     * @throws ServerRejectException if the server rejects the timer
     */
    public void setTimer(Timer timer)
            throws IOException, NetworkException, MalformedResponseException, ParseException,
                    ServerRejectException {
        setTimer(timer, true);
    }

    /**
     * Set the timer
     *
     * @param timer the timer data
     * @param useStrict true to override the current time with the real time
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     * @throws ServerRejectException if the server rejects the timer
     */
    public void setTimer(Timer timer, boolean useStrict)
            throws IOException, NetworkException, MalformedResponseException, ParseException,
                    ServerRejectException {
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
        String requestBody =
                "{\"time_now\": " + now + ", \"time_on\": " + on + ", \"time_off\": " + off + "}";
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
                throw new ServerRejectException(
                        ((Long) parsedJSON.get("code") != null
                                        ? ((Long) parsedJSON.get("code")).toString()
                                        : "0000")
                                + " TmeReject Response:"
                                + response);
            }
        }
    }

    /**
     * Sets mode of Twinkly
     *
     * @param mode Mode to set (OFF, DEMO, MOVIE)
     * @throws ServerRejectException if the server rejects the mode
     * @throws ParseException if the server's response is malformed json
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws NetworkException if a generic network error occurs
     * @throws IOException if a generic I/O error occurs
     */
    @SuppressWarnings("incomplete-switch")
    public void setMode(Mode mode)
            throws IOException, NetworkException, MalformedResponseException, ParseException,
                    ServerRejectException {
        // TODO: rewrite implementation so this is the main method
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
     * Three options are valid: 0: off 1: demo (predefined set of effects that change every so often
     * 2: movie (last uploaded effect) real time is not supported yet an invalid choice will revert
     * to off
     *
     * @throws ParseException if the server's response is malformed json
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws NetworkException if a generic network error occurs
     * @throws IOException if a generic I/O error occurs
     * @throws ServerRejectException if the server rejects the mode
     */
    public void setMode(int option)
            throws IOException, NetworkException, MalformedResponseException, ParseException,
                    ServerRejectException {
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
                if ((long) parsedJSON.get("code") == 1104 && option == 2) {
                    throw new ServerRejectException(
                            ((Long) parsedJSON.get("code") != null
                                            ? ((Long) parsedJSON.get("code")).toString()
                                            : "0000")
                                    + " BadMovie Response:"
                                    + response);
                }
                throw new ServerRejectException(
                        ((Long) parsedJSON.get("code") != null
                                        ? ((Long) parsedJSON.get("code")).toString()
                                        : "0000")
                                + " MdeReject Response:"
                                + response);
            }
        }
    }

    /**
     * Retrieve the current firmware version from the device
     *
     * @return
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     */
    public String getFirmwareVersion()
            throws IOException, NetworkException, MalformedResponseException, ParseException {
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
            throw new NetworkException(
                    "0"
                            + Integer.toString(result)
                            + " Bad_HTTP Response: "
                            + readFromConnection(connection));
        }
    }

    /**
     * Tell the device to reset I guess? The unofficial documentation isn't clear and I didn't test
     * it
     *
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     */
    @Deprecated
    public void reset()
            throws IOException, NetworkException, MalformedResponseException, ParseException {
        login();

        mainURL = new URL("http://" + IP + "/xled/v1/led/reset");
        setupConnection(mainURL, "GET");
        connection.getResponseCode();
    }

    /**
     * Upload and run the effect in the image
     *
     * @param image The image to display. Read the wiki for more information on the file format
     * @param fps The speed to run the animation at, in frames per second
     * @throws IOException if a generic I/O error occurs
     * @throws NetworkException if a generic network error occurs
     * @throws MalformedResponseException if the server's response is malformed HTTP
     * @throws ParseException if the server's response is malformed json
     * @throws ServerRejectException if the server rejects the movie
     */
    public void uploadMovie(File image, int fps)
            throws IOException, NetworkException, MalformedResponseException, ParseException,
                    ServerRejectException {
        login();
        Twinkly lights = gestalt();
        int length = ((Long) lights.getNumberOfLED()).intValue();
        int maxframes = ((Long) lights.getMovieCapacity()).intValue();
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
                System.out.println(
                        "Color at ("
                                + x
                                + ", "
                                + y
                                + ") is rgb("
                                + c.getRed()
                                + ", "
                                + c.getGreen()
                                + ", "
                                + c.getBlue()
                                + ")");
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
            if (parsedJSON.get("code") == null
                    || ((long) parsedJSON.get("code") != 1000
                            && (long) parsedJSON.get("code") != 1100)) {
                throw new ServerRejectException(
                        ((Long) parsedJSON.get("code") != null
                                        ? ((Long) parsedJSON.get("code")).toString()
                                        : "0000")
                                + " MdeReject Response:"
                                + response);
            }
            if (parsedJSON.get("frames_number") == null
                    || ((Long) parsedJSON.get("frames_number")).intValue() != frames) {
                tryAgainLater = true;
            }
        }

        mainURL = new URL("http://" + IP + "/xled/v1/led/movie/config");
        setupConnection(mainURL, "POST");

        OutputStreamWriter wr1 = new OutputStreamWriter(connection.getOutputStream());
        String requestBody =
                "{\"frame_delay\": "
                        + (int) (1000.0 / fps)
                        + ", \"leds_number\": "
                        + length
                        + ", \"frames_number\": "
                        + frames
                        + "}";
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
                throw new ServerRejectException(
                        ((Long) parsedJSON.get("code") != null
                                        ? ((Long) parsedJSON.get("code")).toString()
                                        : "0000")
                                + " CfgReject Response:"
                                + response);
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
                if (parsedJSON.get("code") == null
                        || ((long) parsedJSON.get("code") != 1000
                                && (long) parsedJSON.get("code") != 1100)) {
                    throw new ServerRejectException(
                            ((Long) parsedJSON.get("code") != null
                                            ? ((Long) parsedJSON.get("code")).toString()
                                            : "0000")
                                    + " MdeReject Response:"
                                    + response);
                }
                if (parsedJSON.get("frames_number") == null
                        || (int) parsedJSON.get("frames_number") != frames) {
                    throw new ServerRejectException(
                            ((Long) parsedJSON.get("code") != null
                                            ? ((Long) parsedJSON.get("code")).toString()
                                            : "0000")
                                    + " MovReject Response:"
                                    + response);
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

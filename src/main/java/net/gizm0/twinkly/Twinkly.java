package net.gizm0.twinkly;

import org.json.simple.JSONObject;

/**
 * A class to store the configuration values of Twinkly, populated by a call to {@link
 * TwinklyController#gestalt()}
 */
public class Twinkly {
    private String productName,
            productVersion,
            hardwareVersion,
            ledVersion,
            productCode,
            deviceName,
            uptime,
            hardwareID,
            mac,
            uuid,
            ledProfile,
            copyright;
    private Long flashSize,
            ledType,
            rssi,
            maxSupportedLED,
            baseLEDsNumber,
            numberOfLED,
            frameRate,
            movieCapacity,
            code;

    private JSONObject response;

    /**
     * Parses the response from Twinkly into values
     *
     * @param response a {@link JSONObject} of the data from the response
     */
    public Twinkly(JSONObject response) {
        productName = (String) response.get("product_name");
        productVersion = (String) response.get("product_version");
        hardwareVersion = (String) response.get("hardware_version");
        flashSize = (Long) response.get("flash_size");
        ledType = (Long) response.get("led_type");
        ledVersion = (String) response.get("led_version");
        productCode = (String) response.get("product_code");
        deviceName = (String) response.get("device_name");
        uptime = (String) response.get("uptime");
        rssi = (Long) response.get("rssi");
        hardwareID = (String) response.get("hw_id");
        mac = (String) response.get("mac");
        uuid = (String) response.get("uuid");
        maxSupportedLED = (Long) response.get("max_supported_led");
        baseLEDsNumber = (Long) response.get("base_leds_number");
        numberOfLED = (Long) response.get("number_of_led");
        ledProfile = (String) response.get("led_profile");
        frameRate = (Long) response.get("frame_rate");
        movieCapacity = (Long) response.get("movie_capacity");
        copyright = (String) response.get("copyright");
        code = (Long) response.get("code");
        this.response = response;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getLedVersion() {
        return ledVersion;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getUptime() {
        return uptime;
    }

    public String getHardwareID() {
        return hardwareID;
    }

    public String getMac() {
        return mac;
    }

    public String getUuid() {
        return uuid;
    }

    public String getLedProfile() {
        return ledProfile;
    }

    public String getCopyright() {
        return copyright;
    }

    public long getFlashSize() {
        return flashSize;
    }

    public long getLedType() {
        return ledType;
    }

    public long getRssi() {
        return rssi;
    }

    public long getMaxSupportedLED() {
        return maxSupportedLED;
    }

    public long getBaseLEDsNumber() {
        return baseLEDsNumber;
    }

    public long getNumberOfLED() {
        return numberOfLED;
    }

    public long getFrameRate() {
        return frameRate;
    }

    public long getMovieCapacity() {
        return movieCapacity;
    }

    public long getCode() {
        return code;
    }

    public JSONObject getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return response.toString();
    }
}

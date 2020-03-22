package net.gizm0.twinkly;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/** A class to process image files to be sent to Twinkly */
public class ImageProcessor {
    private int width, height;
    private BufferedImage image = null;
    File input_file = null;

    /**
     * @param input_file The image file to send to the string
     * @param stringLength the number of lights on the string
     * @param maxframes the number of frames in the animation to play
     * @throws IOException if a generic I/O error occurs
     */
    public ImageProcessor(File input_file, int stringLength, int maxframes) throws IOException {
        this.input_file = input_file;
        width = stringLength;
        //		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image = ImageIO.read(input_file);
        if (image.getWidth() != width) {
            throw new IOException("Image is not the same width as string length");
        }
        height = image.getHeight();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getColor(int lightIndex, int frame) {
        return new Color(image.getRGB(lightIndex, frame));
    }

    /*
    	 @Override
    	 public String toString() {
    		String imageString = "";
    		for (int y = 0; y < height; y++) {
    			for (int x = 0; x < width; x++) {
    				Color c = new Color(image.getRGB(x, y));
    				char r = (char) c.getRed();
    				char g = (char) c.getGreen();
    				char b = (char) c.getBlue();
    //				imageString = imageString + r + "" + g + "" + b + "";
    				imageString += c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ",";
    			}
    			imageString += "\n";
    		}
    		imageString = imageString.substring(0, imageString.length() - 2);
    //		String string = "";
    //		int[] imageArray = new int[width * height];
    //		for (int i = 0; i < imageArray.length; i++) {
    //			imageArray[i] = (int) imageString.charAt(i);
    //			string += imageArray[i] + " ";
    //		}
    		return imageString;
    //		return string;
    	}
    	*/

}

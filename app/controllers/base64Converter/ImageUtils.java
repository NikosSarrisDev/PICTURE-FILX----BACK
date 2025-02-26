package controllers.base64Converter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageUtils {

    // Converts an image file to a Base64 encoded string
    public static String convertImageToBase64(String imagePath) throws Exception {
        // Read the image file into a byte array
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));

        // Encode the byte array into a Base64 string
        return Base64.getEncoder().encodeToString(imageBytes);
    }

}

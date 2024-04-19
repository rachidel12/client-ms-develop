package ai.geteam.client.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@Slf4j
public class SignatureUtils {
    public int getSignatureSize(String base64Image){
        try {
            // Decode Base64 string into a byte array
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Get the size of the decoded image
            return imageBytes.length;
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return -1;
        }

    }

    public String getSignatureImageExtention(String base64Image){
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        // Read the magic number (header) of the image format
        byte[] header = new byte[4]; // Adjust the length depending on the image format
        System.arraycopy(imageBytes, 0, header, 0, Math.min(imageBytes.length, header.length));

        // Identify the image format
        String imageExtension = null;
        if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) {
            imageExtension = "jpg";
        } else if (header[0] == (byte)0x89 && header[1] == (byte)0x50 && header[2] == (byte)0x4E && header[3] == (byte)0x47) {
            imageExtension = "png";
        }  // Add more formats as needed

        return imageExtension;
    }
}

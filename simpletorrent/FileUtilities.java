import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FileUtilities {

    private static final int PIECE_SIZE = 512 * 1024; // 512 KB per piece

    // Method to split files in a directory into multiple pieces
    public static Map<String, List<String>> splitFiles(String directoryPath) throws IOException, NoSuchAlgorithmException {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        Map<String, List<String>> fileToPartsMap = new HashMap<>();

        for (File file : files) {
            List<String> partFiles = new ArrayList<>();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[PIECE_SIZE];
            int bytesRead;
            int partCounter = 0;

            while ((bytesRead = fis.read(buffer)) > 0) {
                String fileName = file.getName() + ".part" + partCounter++;
                File newFile = new File(directory, fileName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesRead);
                }

                String checksum = calculateChecksum(buffer);
                System.out.println("Part: " + fileName + " Checksum: " + checksum);
                partFiles.add(fileName);
            }
            fis.close();
            fileToPartsMap.put(file.getName(), partFiles);
        }
        return fileToPartsMap;
    }

    // Calculate SHA-256 checksum for a byte array
    public static String calculateChecksum(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return bytesToHex(hash);
    }

    // Convert byte array to hex string
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

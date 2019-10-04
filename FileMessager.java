import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileMessager {
    public static void sendFile(FileInputStream inputStream, DataOutputStream outputStream) throws IOException {
        transferBytes(inputStream, outputStream);
    }

    public static void receiveFile(DataInputStream inputStream, FileOutputStream outputStream) throws IOException {
        transferBytes(inputStream, outputStream);
    }

    private static void transferBytes(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytes);
        }
    }
}
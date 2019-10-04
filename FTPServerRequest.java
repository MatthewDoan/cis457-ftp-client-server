import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.Runnable;
import java.util.StringTokenizer;

public class FTPServerRequest implements Runnable {
    private Socket socket;

    private DataInputStream dataInputStream;

    private DataOutputStream dataOutputStream;

    public FTPServerRequest(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processRequest() throws Exception {
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = bufferedReader.readLine();
        StringTokenizer tokenizer = new StringTokenizer(request);
        String command = tokenizer.nextToken();
        command = command.toLowerCase();
        System.out.println("Request: " + request);
        if (command.equals("list:") || command.equals("stor:") || command.equals("retr:")) {
            if (command.equals("list:")) {
                listFiles();
            } else if (command.equals("retr:") || command.equals("stor:")) {
                final String fileName = tokenizer.nextToken();
                if (command.equals("retr:")) {
                    retrieveFile(fileName);
                } else {
                    storeFile(fileName);
                }
            }
        }
        closeConnection();
    }

    private void closeConnection() throws Exception {
        System.out.println("Attempting to close streams and socket connection...");
        this.dataInputStream.close();
        this.dataOutputStream.close();
        this.socket.close();
        System.out.println("Successfully closed streams and socket connection.");
    }

    private void retrieveFile(String fileName) throws Exception {
        System.out.println("Attempting to send over " + fileName + "...");
        FileInputStream fileInputStream = new FileInputStream(fileName);
        FileMessager.sendFile(fileInputStream, this.dataOutputStream);
        fileInputStream.close();
        System.out.println(fileName + " was sent to the client.");
    }

    private void storeFile(String fileName) throws Exception {
        System.out.println("Attempting to store " + fileName + "...");
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        FileMessager.receiveFile(this.dataInputStream, fileOutputStream);
        fileOutputStream.close();
        System.out.println(fileName + " was stored.");
    }

    private void listFiles() throws Exception {
        System.out.println("Listing files...");
        String filesList = getCurrentDirectoryFileNames();
        this.dataOutputStream.writeBytes(filesList);
        System.out.println("Sent over file list.");
    }

    private static String getCurrentDirectoryFileNames() {
        System.out.println("Building file list string...");
        StringBuilder stringBuilder = new StringBuilder();
        File currentDirectory = new File(".");
        for (File file : currentDirectory.listFiles()) {
            if (file.isFile()) {
                stringBuilder.append(file.getName() + "\n");
            }
        }
        System.out.println("File list string built.");
        return stringBuilder.toString();
    }
}

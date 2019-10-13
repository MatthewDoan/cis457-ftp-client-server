import java.net.InetAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Runnable;
import java.util.StringTokenizer;

public class FTPServerRequest implements Runnable {

    private Socket controlSocket;

    private Socket dataSocket;

    private DataInputStream dataInputStream;

    private DataOutputStream dataOutputStream;

    private InetAddress clientAddress;

    private final static int CLIENT_DATA_PORT = 1053;

    public FTPServerRequest(Socket controlSocket) {
        this.controlSocket = controlSocket;
        this.clientAddress = controlSocket.getInetAddress();
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processRequest() throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
        while (true) {
            String request = bufferedReader.readLine();
            StringTokenizer tokenizer = new StringTokenizer(request);
            String command = tokenizer.nextToken();
            command = command.toLowerCase();
            System.out.println("Request: " + request);
            if (command.equals("list:") || command.equals("stor:") || command.equals("retr:")) {
                startDataConnection();
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
                closeDataConnection();
            } else if (command.equals("quit")) {
                break;
            }
        }

        bufferedReader.close();
        controlSocket.close();
    }

    private void startDataConnection() throws Exception {
        Thread.sleep(2000);
        dataSocket = new Socket(clientAddress, CLIENT_DATA_PORT);
        dataInputStream = new DataInputStream(dataSocket.getInputStream());
        dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());
    }

    private void closeDataConnection() throws IOException {
        dataInputStream.close();
        dataOutputStream.close();
        dataSocket.close();
    }

    private void retrieveFile(String fileName) throws Exception {
        System.out.println("Attempting to send over " + fileName + "...");
        FileInputStream fileInputStream = new FileInputStream(fileName);
        FileMessager.sendFile(fileInputStream, dataOutputStream);
        fileInputStream.close();
        System.out.println(fileName + " was sent to the client.");
    }

    private void storeFile(String fileName) throws Exception {
        System.out.println("Attempting to store " + fileName + "...");
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        FileMessager.receiveFile(dataInputStream, fileOutputStream);
        fileOutputStream.close();
        System.out.println(fileName + " was stored.");
    }

    private void listFiles() throws Exception {
        System.out.println("Listing files...");
        String filesList = getCurrentDirectoryFileNames();
        dataOutputStream.writeBytes(filesList);
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

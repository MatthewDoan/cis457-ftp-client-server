import java.net.Socket;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.lang.StringBuilder;

public class FTPClient {
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Socket socket;

    public void start() {
        tryStart();
    }

    private void tryStart() {
        try {
            startRepl();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void startRepl()  throws Exception {
        final Scanner scanner = new Scanner(System.in);
        while (true) {
            final String inputLine = scanner.nextLine();
            final StringTokenizer tokenizer = new StringTokenizer(inputLine);
            String command = tokenizer.nextToken();
            command = command.toLowerCase();

            if (command.equals("connect")) {
                final String host = tokenizer.nextToken();
                final int port = Integer.parseInt(tokenizer.nextToken());
                connect(host, port);
            } else if (command.equals("list:") || command.equals("stor:") || command.equals("retr:")) {
                this.dataOutputStream.writeBytes(withNewLine(inputLine));
                if (command.equals("list:")) {
                    listFiles();
                }
                if (command.equals("retr:") || command.equals("stor:")) {
                    final String fileName = tokenizer.nextToken();
                    if (command.equals("retr:")) {
                        retrieveFile(fileName);
                    } else {
                        storeFile(fileName);
                    }
                }
                closeConnection();
            } else if (command.equals("quit")) {
                if (socket.isConnected()) {
                    closeConnection();
                }
                break;
            } else if (command.equals("help")){
                printHelp();
            } else {
                System.out.println("Invalid command!");
            }
        }
        scanner.close();
    }

    private void printHelp() {
        System.out.println("help - show help");
        System.out.println("connect <host> <port> - Connect to a FTPServer at <host> and <port>");
        System.out.println("list: - show list of files stored on FTPServer");
        System.out.println("stor: <file> - store a <file> on the server from the current directory");
        System.out.println("retr: <file> - download a <file> on the server to the current directory");
        System.out.println("quit - exit FTPClient");
    }

    private String withNewLine(String string) {
        return String.format("%s\n", string);
    }

    private void closeConnection() throws Exception {
        this.socket.close();
    }

    private void connect(String host, int port) throws IOException {
        System.out.println("Attempting to connect to server...");
        InetAddress address = InetAddress.getByName(host);
        this.socket = new Socket(address, port);
        this.dataInputStream = new DataInputStream(this.socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
        System.out.println("Connected to server.");
    }

    private void retrieveFile(String fileName) throws IOException {
        System.out.println("Attempting to retrieve " + fileName + "...");
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        FileMessager.receiveFile(this.dataInputStream, fileOutputStream);
        fileOutputStream.close();
        System.out.println(fileName + " was successfully downloaded.");
    }

    private void storeFile(String fileName) throws IOException {
        System.out.println("Attempting to send " + fileName + "...");
        FileInputStream fileInputStream = new FileInputStream(fileName);
        FileMessager.sendFile(fileInputStream, this.dataOutputStream);
        fileInputStream.close();
        System.out.println(fileName + " was successfully stored.");
    }

    private void listFiles() throws IOException {
        System.out.println("Attempting to get list of files on server...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(withNewLine(line));
        }
        System.out.println("List of files on FTP server: ");
        System.out.print(stringBuilder.toString());
    }

    public static void main(String[] args) throws Exception { 
        FTPClient client = new FTPClient();
        client.start();
    }
}
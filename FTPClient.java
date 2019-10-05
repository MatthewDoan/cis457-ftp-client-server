import java.net.ServerSocket;
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
    private DataOutputStream controlOutputStream;

    private ServerSocket serverSocket;
    private Socket controlSocket;
    private Socket dataSocket;

    private final static int CLIENT_DATA_PORT = 1053;

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

    private void startRepl() throws Exception {
        printWelcome();
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
                if (controlSocket.isClosed()) {
                    System.out.println("Connect to a server first!");
                    continue;
                } else {
                    controlOutputStream.writeBytes(withNewLine(inputLine));
                    startDataConnection();
                }
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
                if (controlSocket.isConnected()) {
                    closeControlConnection();
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

    private void printWelcome() {
        System.out.println("Welcome to FTPClient! Use command 'help' for FTPClient usage.");
    }

    private void startDataConnection() throws IOException {
        serverSocket = new ServerSocket(CLIENT_DATA_PORT);
        dataSocket = serverSocket.accept();
        dataInputStream = new DataInputStream(dataSocket.getInputStream());
        dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());
    }

    private void closeDataConnection() throws IOException {
        dataInputStream.close();
        dataOutputStream.close();
        dataSocket.close();
        serverSocket.close();
    }

    private void connect(String host, int port) throws IOException {
        System.out.println("Attempting to connect to server...");
        InetAddress address = InetAddress.getByName(host);
        controlSocket = new Socket(address, port);
        controlOutputStream = new DataOutputStream(controlSocket.getOutputStream());
        System.out.println("Connected to server.");
    }

    private void closeControlConnection() throws IOException {
        controlOutputStream.close();
        controlSocket.close();
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

    private void retrieveFile(String fileName) throws IOException {
        System.out.println("Attempting to retrieve " + fileName + "...");
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        FileMessager.receiveFile(dataInputStream, fileOutputStream);
        fileOutputStream.close();
        System.out.println(fileName + " was successfully downloaded.");
    }

    private void storeFile(String fileName) throws IOException {
        System.out.println("Attempting to send " + fileName + "...");
        FileInputStream fileInputStream = new FileInputStream(fileName);
        FileMessager.sendFile(fileInputStream, dataOutputStream);
        fileInputStream.close();
        System.out.println(fileName + " was successfully stored.");
    }

    private void listFiles() throws IOException {
        System.out.println("Attempting to get list of files on server...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
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
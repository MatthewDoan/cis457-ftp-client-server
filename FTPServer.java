import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.lang.Thread;

public class FTPServer {

    private ServerSocket serverSocket;

    public FTPServer(int port) throws Exception {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true);
        System.out.println("Starting FTPServer on port " + port);
    }

    public void start() {
        try {
            tryStart();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void tryStart()  throws IOException {
        while (true) {
            Socket connection = serverSocket.accept();
            System.out.println("A new client has been accepted. Creating a new thread...");
            FTPServerRequest request = new FTPServerRequest(connection);
            Thread thread = new Thread(request);
            thread.start();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = checkPortArg(args);
        if (port > -1) {
            FTPServer server = new FTPServer(port);
            server.tryStart();
        } else {
            System.exit(1);
        }
    }

    private static int checkPortArg(String[] args) {
        int port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
            System.out.println("First arg must be a number");
        }

        if (port > -1 && port <= 65535) {
            return port;
        } else {
            System.out.println("Invalid port number given");
            return -1;
        }
    }
}
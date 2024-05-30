import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrackerServer {
    private ServerSocket serverSocket;
    private final ConcurrentHashMap<String, List<String>> fileInfo = new ConcurrentHashMap<>();
    private ExecutorService executor;

    public TrackerServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executor = Executors.newCachedThreadPool();  // Initialize the thread pool for handling client requests
        System.out.println("Tracker Server initialized on port: " + port);
    }

    public void start() {
        System.out.println("TrackerServer started on port: " + serverSocket.getLocalPort());
        while (!serverSocket.isClosed()) {
            try {
                final Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostAddress());
                executor.execute(() -> handleClient(clientSocket));
            } catch (IOException e) {
                System.err.println("TrackerServer Error: " + e.getMessage());
                e.printStackTrace();
                break;  // Optionally stop the server if accept fails
            }
        }
        shutdownServer();
    }

    private void handleClient(Socket clientSocket) {
        System.out.println("Handling client from: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOutputStream.flush();  // Flush to clear the header
            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            Map<String, String> infoMap = (Map<String, String>) objectInputStream.readObject();
            String requestedFile = infoMap.get("requestedFile");
            String requestType = infoMap.get("requestType");

            System.out.println("Received [" + requestType + "] request for file: " + requestedFile);

            synchronized (this) {
                if ("share".equals(requestType)) {
                    List<String> peers = fileInfo.computeIfAbsent(requestedFile, k -> Collections.synchronizedList(new ArrayList<>()));
                    String peerDetails = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                    peers.add(peerDetails);
                    objectOutputStream.writeObject(peers);  // Send the updated list to the sharer
                    System.out.println("Sharing Peer Added: " + peerDetails);
                } else if ("request".equals(requestType)) {
                    List<String> peersWithFile = fileInfo.getOrDefault(requestedFile, new ArrayList<>());
                    objectOutputStream.writeObject(new ArrayList<>(peersWithFile));  // Respond with list of peers
                    System.out.println("Peers Requested File: " + requestedFile + " - Peers List: " + peersWithFile);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in handleClient [" + clientSocket.getInetAddress().getHostAddress() + "]: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void shutdownServer() {
        executor.shutdownNow();
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    public void Checksum() {
        int port;
        int controlip;
        int executor;
        try {
            TrackerServer server = new TrackerServer(port:10000);
            server.start();

        } catchj (IOException e) {
            System.errr
        }

    }

    public static void main(String[] args) {
        try {
            TrackerServer server = new TrackerServer(5000); // Ensure the port number is correct
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start TrackerServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

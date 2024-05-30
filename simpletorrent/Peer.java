import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class CreateServer extends Thread {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, File> sharedFiles; // Maps file names to File objects for seeding

    public CreateServer(ServerSocket serverSocket, ConcurrentHashMap<String, File> sharedFiles) {
        this.serverSocket = serverSocket;
        this.sharedFiles = sharedFiles;
    }

    @Override
    public void run() {
        System.out.println("Server listening on port: " + serverSocket.getLocalPort());
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                new HandlerConversation(socket, sharedFiles).start();
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
}

class HandlerConversation extends Thread {
    private Socket socket;
    private ConcurrentHashMap<String, File> sharedFiles;

    public HandlerConversation(Socket socket, ConcurrentHashMap<String, File> sharedFiles) {
        this.socket = socket;
        this.sharedFiles = sharedFiles;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            String command = dis.readUTF();
            if ("DOWNLOAD_REQUEST".equals(command)) {
                String fileName = dis.readUTF();
                sendFile(fileName, dos);
            }
        } catch (IOException e) {
            System.err.println("Error in communication: " + e.getMessage());
        }
    }

    private void sendFile(String fileName, DataOutputStream dos) {
        File file = sharedFiles.get(fileName);
        if (file == null || !file.exists()) {
            System.err.println("File not found: " + fileName);
            return;
        }
        final int PIECE_SIZE = 4096;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[PIECE_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.writeInt(bytesRead);
                dos.write(buffer, 0, bytesRead);
                dos.flush();
            }
            dos.writeInt(0); // Send zero to indicate no more data
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
        }
    }
}

public class Peer {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, File> sharedFiles = new ConcurrentHashMap<>();
    private Socket trackerSocket;
    private ObjectInputStream trackerInput;
    private ObjectOutputStream trackerOutput;

    public Peer(String trackerHost, int trackerPort) throws IOException {
        this.serverSocket = new ServerSocket(0); // Listen on any free port
        System.out.println("Peer server initialized on port: " + serverSocket.getLocalPort());
        connectToTracker(trackerHost, trackerPort);
        new CreateServer(serverSocket, sharedFiles).start(); // Start handling incoming connections
    }

    private void connectToTracker(String host, int port) throws IOException {
        trackerSocket = new Socket(host, port);
        trackerOutput = new ObjectOutputStream(trackerSocket.getOutputStream());
        trackerInput = new ObjectInputStream(trackerSocket.getInputStream());
        System.out.println("Connected to Tracker at " + host + ":" + port);
    }

    public void sendTrackerRequest(String type, String fileName) throws IOException {
        HashMap<String, String> request = new HashMap<>();
        request.put("requestType", type);
        request.put("requestedFile", fileName);
        trackerOutput.writeObject(request);
        trackerOutput.flush();
    }

    public void handleTrackerResponse() throws IOException, ClassNotFoundException {
        List<String> peerList = (List<String>) trackerInput.readObject();
        System.out.println("Peers with the file: " + peerList);
    }

    public void addFileToShare(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            sharedFiles.put(file.getName(), file);
            System.out.println("Added file for sharing: " + file.getName());
        } else {
            System.out.println("File does not exist: " + filePath);
        }
    }

    public void requestFile(String host, int port, String fileName) {
        try (Socket socket = new Socket(host, port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            dos.writeUTF("DOWNLOAD_REQUEST");
            dos.writeUTF(fileName);

            File file = new File(fileName);
            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                int bytesRead;
                byte[] buffer = new byte[4096];

                while ((bytesRead = dis.readInt()) > 0) {
                    buffer = new byte[bytesRead];
                    dis.readFully(buffer);
                    bos.write(buffer);
                }
                bos.flush();
            }

            System.out.println("File downloaded successfully: " + fileName);

        } catch (IOException e) {
            System.err.println("Error requesting file: " + e.getMessage());
        }
    }

    public void shutdown() {
        try {
            System.out.println("Shutting down the peer server.");
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error shutting down server socket: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Peer peer = new Peer("localhost", 5000);
            runCLI(peer);
        } catch (Exception e) {
            System.err.println("Failed to start the Peer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCLI(Peer peer) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter command (share, request, exit):");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(" ");
            String command = parts[0];

            switch (command.toLowerCase()) {
                case "share":
                    if (parts.length > 1) peer.addFileToShare(parts[1]);
                    break;
                case "request":
                    if (parts.length > 3)
                        peer.requestFile(parts[1], Integer.parseInt(parts[2]), parts[3]);
                    break;
                case "exit":
                    peer.shutdown();
                    scanner.close();
                    return;
                default:
                    System.out.println("Unknown command.");
                    break;
            }
        }
    }
}

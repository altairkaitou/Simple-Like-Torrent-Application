import java.util.Scanner;

public class TorrentCLI {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            Peer peer = new Peer("localhost", 5000);  // Initialize the peer using your existing Peer class

            boolean running = true;
            while (running) {
                System.out.println("\nTorrent Management System");
                System.out.println("1. Share a file");
                System.out.println("2. Request a file");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        System.out.print("Enter the full path of the file to share: ");
                        String filePath = scanner.nextLine();
                        peer.addFileToShare(filePath);
                        break;
                    case 2:
                        System.out.print("Enter the host of the file provider: ");
                        String host = scanner.nextLine();
                        System.out.print("Enter the port number of the file provider: ");
                        int port = scanner.nextInt();
                        scanner.nextLine();  // Consume newline
                        System.out.print("Enter the file name to request: ");
                        String fileName = scanner.nextLine();
                        peer.requestFile(host, port, fileName);
                        break;
                    case 3:
                        running = false;
                        peer.shutdown();
                        System.out.println("Exiting the Torrent Management System.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}

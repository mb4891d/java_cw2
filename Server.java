package client_server;
 


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    // List to store connected members (clients). Flexible. 
    private static List<Member> members = Collections.synchronizedList(new ArrayList<>());
    // Current coordinator
    private static Member coordinator = null;
    // File to store coordinator information. 
    private static final String COORDINATOR_FILE = "coordinator.txt";
    // Interval for sending updates to coordinator. This will only be shown in coord window. 
    private static final int UPDATE_INTERVAL = 20000; // Update interval in milliseconds

    public static void main(String[] args) throws Exception {
        loadCoordinator(); // Load coordinator information from file
        ServerSocket serverSocket = new ServerSocket(3000);
        System.out.println("Server ready to accept connections.");

        // Start the timer to send periodic updates to the coordinator
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateTask(), 0, UPDATE_INTERVAL);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket);
            new ClientHandler(socket).start();
        }
    }

    // Load coordinator information from file
    private static void loadCoordinator() {
        try {
            File file = new File(COORDINATOR_FILE);
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("Created coordinator file: " + COORDINATOR_FILE);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String id = reader.readLine();
                if (id != null) {
                    coordinator = new Member(id, null); // Create a dummy coordinator
                    System.out.println("Coordinator loaded: " + coordinator.id);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save coordinator information to file
    private static void saveCoordinator() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COORDINATOR_FILE))) {
            if (coordinator != null) {
                writer.println(coordinator.id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Member class representing connected clients
    static class Member {
        String id; // Unique identifier for each member
        Socket socket;
        BufferedReader reader;
        PrintWriter writer;

        public Member(String id, Socket socket) throws IOException {
            this.id = id;
            this.socket = socket;
            // Input stream to read data from the client
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Output stream to send data to the client
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        }
    }

    // ClientHandler thread responsible for handling each client connection
    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private Member member;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                // Read client ID
                String id = reader.readLine();

                // Create member and add to the list
                member = new Member(id, socket);
                synchronized (members) {
                    members.add(member);
                }

                // If this is the first member, make it the coordinator
                if (coordinator == null) {
                    coordinator = member;
                    System.out.println("Coordinator set: " + coordinator.id);
                    saveCoordinator(); // Save coordinator information
                }

                // Inform member about coordinator
                member.writer.println("Coordinator: " + coordinator.id);

                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Message received from " + member.id + ": " + message);
                    if (message.startsWith("@")) { // Check if it's a private message
                        String[] parts = message.split(":", 2);
                        String recipientId = parts[0].substring(1); // Extract recipient ID
                        String privateMessage = parts[1].trim();
                        sendMessageToRecipient(privateMessage, recipientId);
                    } else {
                        broadcastMessage(message); // Broadcast message to all clients
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally { //This part of the code assigns the coordinator and reassigns if they have left. 
                // Clean up resources
                if (member != null) {
                    synchronized (members) {
                        members.remove(member);
                    } //The next part goes through the list if the members list is not empty. 
                    if (member == coordinator) {
                        coordinator = members.isEmpty() ? null : members.get(0);
                        if (coordinator != null) {
                            System.out.println("New coordinator set: " + coordinator.id);
                            saveCoordinator(); // Save coordinator information
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Broadcast message to all clients
        private void broadcastMessage(String message) {
            synchronized (members) {
                for (Member m : members) {
                    m.writer.println("Client " + member.id + ": " + message);
                }
            }
        }

        // Send private message to a specific client
        private void sendMessageToRecipient(String message, String recipientId) {
            synchronized (members) {
                for (Member m : members) {
                    if (m.id.equals(recipientId)) {
                        m.writer.println("Private message from " + member.id + ": " + message);
                        return;
                    }
                }
            }
            member.writer.println("Recipient not found: " + recipientId);
        }
    }

    // Timer task to send periodic updates to the coordinator. task complete. 
    static class UpdateTask extends TimerTask {
        @Override
        public void run() {
            synchronized (members) {
                if (coordinator != null) {
                    coordinator.writer.println("Active clients: " + members.size());
                }
            }
        }
    }
}
package client_server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

// Main Client class
public class Client {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(); // Create GUI instance
            gui.createAndShowGUI(); // Display GUI
        });
    }
}

// GUI Class for the Client. Contains all buttons and input areas. 
class ClientGUI {
    private JTextField idField; // Input field for client ID
    private JTextField serverIPField; // Input field for server IP
    private JTextField serverPortField; // Input field for server port
    private JTextField recipientField; // Input field for recipient ID
    private JTextArea messageArea; // Text area for message input
    private JTextArea infoArea; // Text area for server info display
    private Socket socket; // Socket for communication
    private PrintWriter writer; // Writer to send data to server
    private BufferedReader reader; // Reader to read data from server
    private String id; // Client ID
    private String serverIP; // Server IP
    private int serverPort; // Server port
    private String recipientId; // Recipient ID for private messages

    // Method to create and display GUI. This has all info entry's. 
    public void createAndShowGUI() {
        JFrame frame = new JFrame("Client"); // Create a frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation

        JPanel mainPanel = new JPanel(); // Main panel for layout
        mainPanel.setLayout(new BorderLayout()); // Set layout to BorderLayout

        JPanel setupPanel = new JPanel(); // Panel for setup details
        setupPanel.setLayout(new GridLayout(5, 2)); // Set layout to grid

        // Components for client ID
        JLabel idLabel = new JLabel("Enter your ID:");
        idField = new JTextField("1"); // Default to client 1
        setupPanel.add(idLabel);
        setupPanel.add(idField);

        // Components for server IP
        JLabel serverIPLabel = new JLabel("Enter server IP:");
        serverIPField = new JTextField("127.0.0.1");
        setupPanel.add(serverIPLabel);
        setupPanel.add(serverIPField);

        // Components for server port
        JLabel serverPortLabel = new JLabel("Enter server port:");
        serverPortField = new JTextField("3000");
        setupPanel.add(serverPortLabel);
        setupPanel.add(serverPortField);

        // Components for recipient ID
        JLabel privateMember = new JLabel("Enter recipient ID:");
        recipientField = new JTextField(); // Added recipient field
        setupPanel.add(privateMember);
        setupPanel.add(recipientField);

        // Button to connect to server
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ConnectButtonListener());
        setupPanel.add(connectButton);

        // Add setup panel to main panel
        mainPanel.add(setupPanel, BorderLayout.NORTH);

        // Text area to display server info
        infoArea = new JTextArea(5, 30);
        infoArea.setEditable(false);
        JScrollPane infoScrollPane = new JScrollPane(infoArea);
        mainPanel.add(infoScrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(); // Panel for message sending
        messagePanel.setLayout(new BorderLayout());

        // Label for message input
        JLabel messageLabel = new JLabel("Enter Message:");
        messagePanel.add(messageLabel, BorderLayout.NORTH);

        // Text area for message input
        messageArea = new JTextArea(5, 30);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        // Button to send message
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        messagePanel.add(sendButton, BorderLayout.SOUTH);

        // Add message panel to main panel
        mainPanel.add(messagePanel, BorderLayout.SOUTH);

        // Add main panel to frame
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setVisible(true); // Make frame visible
    }

    // Action listener for Connect button
    private class ConnectButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                id = idField.getText();
                serverIP = serverIPField.getText();
                serverPort = Integer.parseInt(serverPortField.getText());
                connectToServer(id, serverIP, serverPort); // Connect to server
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to connect to server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Action listener for Send button
    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = messageArea.getText();
            recipientId = recipientField.getText(); // Get recipient ID from text field
            sendMessage(message, recipientId); // Send message
        }
    }

    // Method to connect to server
    private void connectToServer(String id, String serverIP, int serverPort) {
        try {
            socket = new Socket(serverIP, serverPort); // Create socket
            writer = new PrintWriter(socket.getOutputStream(), true); // Create writer
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Create reader

            writer.println(id); // Send client ID to server

            // Start a thread to continuously receive messages from server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = reader.readLine()) != null) {
                        infoArea.append(serverMessage + "\n"); // Display server messages
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error receiving message from server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }  //This helps catch any errors and maintain the program. 
            }).start();

            JOptionPane.showMessageDialog(null, "Connected to server.", "Success", JOptionPane.INFORMATION_MESSAGE);
            	//We used this part specifically as it was a clear sign everything works. 
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to send message to server
    private void sendMessage(String message, String recipientId) {
        if (writer != null) {
            if (recipientId.isEmpty()) {
                // If no recipient specified, send message to coordinator
                writer.println(message);
            } else {
                // Send private message to the specified recipient
                writer.println("@" + recipientId + ": " + message);
            }
            messageArea.setText(""); // Clear the message input field after sending
        }
    }
}

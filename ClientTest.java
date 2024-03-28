package Testing;

import client_server.Client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {

    private ClientGUI clientGUI;

    @BeforeEach
    public void setUp() {
        clientGUI = new ClientGUI();
    }

    @Test
    public void testConnectToServer() {
        // Test connecting to the server with valid parameters
        assertDoesNotThrow(() -> clientGUI.connectToServer("1", "127.0.0.1", 3000));

        // Test connecting to the server with invalid parameters
        assertThrows(Exception.class, () -> clientGUI.connectToServer("", "invalidIP", 9999));
    }

    @Test
    public void testSendMessage() {
        // Test sending a message without specifying a recipient ID
        assertDoesNotThrow(() -> clientGUI.sendMessage("Test message", ""));

        // Test sending a message with a recipient ID
        assertDoesNotThrow(() -> clientGUI.sendMessage("Private message", "2"));
    }
}


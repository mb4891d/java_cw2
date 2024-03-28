
package Testing;
import client_server.Server;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

   @BeforeEach
   public void setUpStreams() {
       // Sets up any necessary resources before each test
   }

   @AfterEach
   public void restoreStreams() {
       // Cleans up resources after each test
   }

   @Test
   public void testLoadCoordinator() throws IOException {
       // Creates temporary coordinator file and writes some content into it
       File file = new File("coordinator.txt");
       try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
           writer.println("testCoordinator");
       }

       // Calls loadCoordinator method
       server.loadCoordinator();

       // Checks if coordinator is loaded correctly
       assertNotNull(server.coordinator);
       assertEquals("testCoordinator", server.coordinator.id);

       // Deletes coordinator file after the test
       file.delete();
   }
  
}
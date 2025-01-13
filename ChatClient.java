
package ChatAppAssignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    // Define the IP address and port of the chat server
    public static void main(String[] args) {
        final String SERVER_IP = "localhost";
        final int SERVER_PORT = 5566;

        try (
            // Create a socket to connect to the chat server
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            // Set up input streams to read from the console and from the server
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Set up an output stream to send messages to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true) ) {
            System.out.println("Connected to server. Type '\\q' to quit.");

            // Start a separate thread to listen for incoming messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;

                    // Continuously read and display messages from the server
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Accept user input and send messages to the server
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                // Send the user's message to the server
                out.println(userMessage);

                // Break the loop if the user enters the termination command
                if (userMessage.equals("\\q")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to connect to server.");
            e.printStackTrace();
        }
    }
}

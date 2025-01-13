package ChatAppAssignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

   
    private static final int PORT = 5566;

    // Maintain a list of connected clients
    private static final List<ClientHandler> clients = new ArrayList<>();

    // Create a fixed-size thread pool to handle multiple clients concurrently
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                try {
                    // Accept incoming client connections
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Create a new ClientHandler thread for each connected client
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clients);

                    // Add the new client handler to the list of clients
                    clients.add(clientHandler);

                    // Submit the client handler thread to the thread pool for execution
                    executorService.submit(clientHandler);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            // Print error message if the server fails to start
            System.err.println("Couldn't start server");
        } finally {
            // Shut down the thread pool when the server is done
            executorService.shutdown();
        }
    }

    // Inner class representing a client handler thread
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final List<ClientHandler> clients;
        private PrintWriter out;

        // Constructor to initialise the client handler with a client socket and the list of clients
        public ClientHandler(Socket clientSocket, List<ClientHandler> clients) {
            this.clientSocket = clientSocket;
            this.clients = clients;
        }

        @Override
        public void run() {
            try {
                // Input and output streams for communication with the client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Broadcast message to all clients when a new user joins
                broadcast("New user joined: " + clientSocket.getInetAddress().getHostAddress());

                String message;

                // Continuously read messages from the client
                while ((message = in.readLine()) != null) {
                    if (message.equals("\\q")) {
                        break;
                    }
                    // Broadcast received message to all clients
                    broadcast("[" + clientSocket.getInetAddress().getHostAddress() + "]: " + message);
                }

            } catch (IOException e) {
                if (!clientSocket.isClosed()) {
                    e.printStackTrace();
                }
            } finally {
                try {
                    // Close the client socket and remove the client handler from the list of clients
                    clientSocket.close();
                    clients.remove(this);

                    // Broadcast a message when a user leaves
                    broadcast("User left: " + clientSocket.getInetAddress().getHostAddress());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Helper method to broadcast a message to all connected clients
        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }

        // Helper method to send a message to the client associated with this handler
        private void sendMessage(String message) {
            out.println(message);
        }
    }
}



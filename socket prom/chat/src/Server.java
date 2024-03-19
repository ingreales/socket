import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Clients {
    private static final int DEFAULT_PORT = 5050;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        Clients server = new Clients();
        server.start();
    }

    public void start() {
        int port = getPortFromUser();
        try {
            serverSocket = new ServerSocket(port);
            displayMessage("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                displayMessage("Client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            displayError("Error starting the server: " + e.getMessage());
        } finally {
            closeServer();
        }
    }

    private int getPortFromUser() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        displayMessage("Enter port number [" + DEFAULT_PORT + "]: ");
        try {
            String input = reader.readLine();
            if (input.isEmpty())
                return DEFAULT_PORT;
            return Integer.parseInt(input);
        } catch (IOException | NumberFormatException e) {
            displayError("Invalid port number, using default port " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

    private void displayMessage(String message) {
        System.out.println(message);
    }

    private void displayError(String errorMessage) {
        System.err.println(errorMessage);
    }

    private void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                displayMessage("Server closed.");
            }
        } catch (IOException e) {
            displayError("Error closing the server: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("exit")) {
                        break;
                    }
                    System.out.println("Client: " + inputLine);
                    String response = "Server: " + inputLine.toUpperCase();
                    out.println(response);
                }

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

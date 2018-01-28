import java.io.*;
import java.net.*;
import java.util.*;
/**
 * This handler deals with each client request.
 * @author Xiaoxiong Li, Di Wu
 *
 */
public class ClientHandler implements Runnable {
    /**
     * The client socket that this handler needs to serve.
     */
    private Socket clientSocket;
    /**
     * The reader that reads input from the client.
     */
    private BufferedReader reader;
    /**
     * The file path the the client requests.
     */
    private String filePath;
    /**
     * The headers from the request.
     */
    private Map<String, String> headers;
    /**
     * Constructor using the client socket.
     * @param socket the client socket
     */
    public ClientHandler(Socket socket) {
        try {
            clientSocket = socket;
            reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            headers = new HashMap<>();
        }
        catch (IOException e) {
            System.err.println("Read failed.");
            System.exit(1);
        }
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub

    }
    /**
     * Parse the request to get file path and headers.
     */
    
}

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
     * The writer that output to the client.
     */
    private PrintWriter writer;
    /**
     * Method token.
     */
    private String method;
    /*
     * URI.
     */
    private String uri;
    /**
     * Http version, 1: http/1.1, 0: http/1.0.
     */
    private int version;
    /**
     * The file path the the client requests.
     */
    private String filePath;
    /**
     * The host of the file.
     */
    private String host;
    /**
     * Port.
     */
    private int port;
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
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
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
    public void parse() throws Exception {
        /**
         * Parse the request to get file path and headers.
         */
        String inputLine;
        
        //Parse the first the request line
        inputLine = reader.readLine();
        String[] line = inputLine.split("\\s+");
        if (line.length != 3) {
            writer.println("400 Bad Request");
            throw new Exception();
        }
        method = line[0];
        uri = line[1];
        
        //parse HTTP version
        if (line[2].compareTo("HTTP/1.1") == 0)
            version = 1;
        else if (line[2].compareTo("HTTP/1.0") == 0)
            version = 0;
        else {
            writer.println("505 HTTP Version Not Supported");
            throw new Exception();
        }
        
        //parse URI to get host, port and path
        if (uri.indexOf("http://") == 0)
            uri = uri.substring(7);
        else if (uri.indexOf("http://") > 0) {
            writer.println("400 Bad Request");
            throw new Exception();
        }
        int portIdx;
        if ((portIdx = uri.indexOf(':')) != -1) {
            host = uri.substring(0, portIdx);
            int pathIdx = uri.indexOf('/');
            if (pathIdx <= portIdx + 1 && pathIdx != -1) {
                writer.println("400 Bad Request");
                throw new Exception();
            } else if (pathIdx > portIdx + 1) {
                port = Integer.valueOf(uri.substring(portIdx + 1, pathIdx));
                filePath = uri.substring(pathIdx);
            } else {
                port = Integer.valueOf(uri.substring(portIdx + 1));
            }
        } else {
            int pathIdx = uri.indexOf('/');
            if (pathIdx != -1) {
                filePath = uri.substring(pathIdx);
            }
        }
    }
}

import java.io.*;
import java.net.*;
import java.util.*;
/**
 * This handler deals with each client request.
 * @author Xiaoxiong Li, Di Wu
 *
 */
public class ClientHandler implements Runnable {
    private static final int MAX_BUFFER = 2048;
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
     * The file requested.
     */
    private File file;
    /**
     * Constructor using the client socket.
     * @param socket the client socket
     */
    public ClientHandler(Socket socket) {
        try {
            clientSocket = socket;
            reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            headers = new HashMap<>();
        }
        catch (IOException e) {
            System.err.println("Read failed.");
            Thread.currentThread().interrupt();
        }
    }
    @Override
    public void run() {
        //parse request
        try {
            parse();
        }
        catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        //find the file
        try {
            findFile();
        }
        catch (FileNotFoundException e) {
            writer.println("404 Not Found");
            Thread.currentThread().interrupt();
        }
        sendResponse();
        //send back the file
        try {
            sendFile();
        }
        catch (IOException ex) {
            writer.println("500 Internal Server Error");
            Thread.currentThread().interrupt();
        }
    }
    private void parse() throws Exception {
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
	//Parse headers.
        while ((inputLine = reader.readLine()) != null) {
            if (inputLine.length() == 0) {
                break;
            }
            int colonIdx;
            colonIdx = inputLine.indexOf(':');
            if (colonIdx == -1)
                continue;
            else
                headers.put(inputLine.substring(0, colonIdx), inputLine.substring(colonIdx + 1));
        }
        //Parse request entity.
        while ((inputLine = reader.readLine()) != null) {
        }
    }
    /**
     * Find the file.
     * @throws FileNotFoundException 
     */
    private void findFile() throws FileNotFoundException {
        String absolutePath = System.getProperty("user.dir") + "/content" + filePath;
        System.out.println(absolutePath);
        file = new File(absolutePath);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
    }
    /**
     * Send response to the client.
     */
    private void sendResponse() {
        String httpDate = 
        writer.println("HTTP/1.1 200 OK");
        writer.println("Date: ")
        writer.println("");
    }
    /**
     * Send file and send it to the client.
     * @throws IOException 
     */
    private void sendFile() throws IOException {
        FileInputStream input = new FileInputStream(file);
        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
        byte[] buffer = new byte[MAX_BUFFER];
        while (input.read(buffer) > 0) {
            output.write(buffer);
            output.flush();
        }
        input.close();
        output.close();
    }
    private String getTime() {
        Calendar calendar = C
    }
}

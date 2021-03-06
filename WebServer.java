import java.io.IOException;
import java.net.*;

/**
 * This web server opens a server socket on port 80, then continually
 * accept client request. Once getting a request, it create a thread to
 * handle it and itself goes on to accept another request.
 * @author Xiaoxiong Li, Di Wu
 *
 */
public class WebServer {
	@SuppressWarnings("resource")
    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("usage: <classname> + <port>");
	    }
	    int port = Integer.valueOf(args[0]);
	    ServerSocket serverSocket = null;
	    //Open a socket on the port
	    try {
	        serverSocket = new ServerSocket(port);
	    }
	    catch (IOException e) {
	        System.err.println("Could not listen on port: " + port + ".");
            System.exit(1);
	    }
	    System.out.println("Web server is working.");
	    while (true) {
	        Socket clientSocket = null;
	        try {
	            clientSocket = serverSocket.accept();
	        }
	        catch (IOException e) {
	            System.err.println("Acception failed.");
	            System.exit(1);
	        }
	        Thread t = new Thread(new ClientHandler(clientSocket));
	        t.start();
	        System.out.println("Got a connection.");
	    }
	}
}

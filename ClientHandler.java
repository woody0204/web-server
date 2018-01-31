import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * This handler deals with each client request.
 * @author Xiaoxiong Li, Di Wu
 *
 */
public class ClientHandler implements Runnable {
    /**
     * Whether the thread needs to stop in advance.
     */
    private boolean isStop;
    /**
     * The size of buffer used to transmit file.
     */
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
            isStop = false;
        }
        catch (IOException e) {
            System.err.println("Read failed.");
            Thread.currentThread().interrupt();
            isStop = true;
        }
    }
    @Override
    public void run() {
        //parse request
        if (!isStop) {
            try {
                parse();
            }
            catch (Exception e) {
                isStop = true;
            }
        }
        //find the file
        if (!isStop) {
            try {
                findFile();
            }
            catch (FileNotFoundException e) {
                writer.println("HTTP/1.1 404 Not Found");
                writer.println("");
                isStop = true;
            }
        }
        if (!isStop) {
            sendResponse();
        }
        writer.close();
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
            writer.println("");
            throw new Exception();
        }
        method = line[0];
        uri = line[1];
        
        //parse HTTP version
        if (!line[2].equals("HTTP/1.1")) {
            writer.println("505 HTTP Version Not Supported");
            writer.println("");
            throw new Exception();
        }
        
        //parse URI to get host, port and path
        if (uri.indexOf("http://") == 0)
            uri = uri.substring(7);
        else if (uri.indexOf("http://") > 0) {
            writer.println("400 Bad Request");
            writer.println("");
            throw new Exception();
        }
        int portIdx;
        if ((portIdx = uri.indexOf(':')) != -1) {
            host = uri.substring(0, portIdx);
            int pathIdx = uri.indexOf('/');
            if (pathIdx <= portIdx + 1 && pathIdx != -1) {
                writer.println("400 Bad Request");
                writer.println("");
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
        
    }
    /**
     * Find the file.
     * @throws FileNotFoundException 
     */
    private void findFile() throws FileNotFoundException {
        String absolutePath = "content" + filePath;
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
        long start = 0, end = file.length() - 1;
        if (headers.containsKey("Range")) { //request contains range
            RangeHeader rangeHeader = 
                    new RangeHeader(headers.get("Range"), file.length());
            if (rangeHeader.isValid()) { //range of header in request is valid
                if (rangeHeader.isSatisfiable()) { //range is satisfiable
                    writer.println("HTTP/1.1 206 Partial Content");
                    writer.println("Date: " + getTime());
                    writer.println("Last-Modified: " + getLastModifiedTime(file));
                    writer.println("Connection: Keep-Alive");
                    writer.println("Accept-Ranges: bytes");
                    List<RangeHeader.Range> listOfRanges = rangeHeader.ranges;
                    if (listOfRanges.size() == 1) {
                        RangeHeader.Range range = listOfRanges.get(0);
                        long lb = range.getStart();
                        long rb = range.getEnd();
                        writer.println("Content-Range: bytes " + 
                        Long.toString(lb) + "-" + 
                        Long.toString(rb) + "/" + 
                        Long.toString(file.length()));
                        writer.println("Content-Length: " + 
                        Long.toString(rb - lb + 1));
                        writer.println("Content-Type: " + parseType(filePath));
                        writer.println("");
                        start = lb;
                        end = rb;
                    }
                } else { //range is not satisfiable
                    writer.println("HTTP/1.1 416 Requested range not satisfiable");
                    writer.println("Date: " + getTime());
                    writer.println("Last-Modified: " + getLastModifiedTime(file));
                    writer.println("Connection: Keep-Alive");
                    writer.println("Accept-Ranges: bytes");
                    writer.println("Content-Range: bytes " + 
                            "*" + "/" + 
                            Long.toString(file.length()));
                    writer.println("Content-Length: " + file.length());
                    writer.println("Content-Type: " + parseType(filePath));
                    writer.println("");
                }
            } else { //range of header in request is not valid
                writer.println("HTTP/1.1 200 OK");
                writer.println("Date: " + getTime());
                writer.println("Last-Modified: " + getLastModifiedTime(file));
                writer.println("Connection: Keep-Alive");
                writer.println("Accept-Ranges: bytes");
                writer.println("Content-Length: " + file.length());
                writer.println("Content-Type: " + parseType(filePath));
                writer.println("");
            }
        } else {  //request doesn't contain range
            writer.println("HTTP/1.1 200 OK");
            writer.println("Date: " + getTime());
            writer.println("Last-Modified: " + getLastModifiedTime(file));
            writer.println("Connection: Keep-Alive");
            writer.println("Accept-Ranges: bytes");
            writer.println("Content-Length: " + file.length());
            writer.println("Content-Type: " + parseType(filePath));
            writer.println("");
        }
        try {
            sendFile(start, end);
        }
        catch (IOException ex) {
            System.out.println("transmit failed");
        }
    }
    /**
     * Send file and send it to the client.
     * @throws IOException 
     */
    private void sendFile(long start, long end) throws IOException {
        RandomAccessFile rFile = new RandomAccessFile(file, "r");
        rFile.seek(start);
//        FileInputStream input = new FileInputStream(file);
        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
        byte[] buffer = new byte[MAX_BUFFER];
//        long itr = start / MAX_BUFFER;
//        int remainder = (int)start % MAX_BUFFER;
//        for (int i = 0; i < itr; i++) {
//            input.read(buffer, 0, MAX_BUFFER);
//        }
//        input.read(buffer, 0, remainder);
//        buffer = new byte[MAX_BUFFER];
        long itr = (end - start + 1) / MAX_BUFFER;
        int remainder = (int)(end - start + 1) % MAX_BUFFER;
        for (int i = 0; i < itr; i++) {
            rFile.read(buffer, 0, MAX_BUFFER);
            output.write(buffer);
            output.flush();
        }
        rFile.read(buffer, 0, remainder);
        output.write(buffer);
        output.flush();
        
        rFile.close();
        output.close();
    }
    
    /**
     * Get the current server time and convert it to http date.
     * @return
     */
    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzz");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(calendar.getTime());
    }

    /**
     * Get the last modified time of the file and convert it to http date.
     * @param f
     * @return
     */
    private String getLastModifiedTime(File f) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzz");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(f.lastModified());
    }
    
    /**
     * Parse the type of the file and return the Content-Type.
     * @param f
     * @return
     */
    private String parseType(String path) {
        int idx = path.lastIndexOf('.');
        if (idx == -1)
            return "application/octet-stream";
        String suffix = path.substring(idx);
        switch (suffix) {
        case ".txt":
            return "text/plain";
        case ".css":
            return "text/css";
        case ".html":
            return "text/html";
        case ".htm":
            return "text/html";
        case ".gif":
            return "image/gif";
        case ".jpg":
            return "image/jpeg";
        case ".jpeg":
            return "image/jpeg";
        case ".png":
            return "image/png";
        case ".js":
            return "application/javascript";
        case ".mp4":
            return "video/mp4";
        case ".webm":
            return "video/webm";
        case ".ogg":
            return "video/ogg";
        default:
            return "application/octet-stream";    
        }
    }
}

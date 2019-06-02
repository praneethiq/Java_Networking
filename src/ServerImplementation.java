import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Date;

/**
 * Implementation of a web server which handles GET and HEAD requests.
 * @author 180029539
 */
public class ServerImplementation implements Runnable {

	private int listeningPort;
	private String webDirectory;
	private ServerSocket serverSocket;
	private InputStream instr;
	private BufferedReader br;
	private BufferedOutputStream brout;
	private PrintWriter pw;
	private OutputStream outstr;
	private String request;
	private String status;
	/**
	 * Gets the information of current port and working web directory.
	 * @param sourceDirectory Source web directory
	 * @param inputPort Listening port
	 * @throws IOException
	 */
	public ServerImplementation(String sourceDirectory,	int inputPort) throws IOException {
		this.listeningPort = inputPort;
		this.webDirectory = sourceDirectory;
	}
	/**
	 * Initialises multiple threads.
	 */
	public void run() {
		try {
			initialiseServer(listeningPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the port is available and initialises the server.
	 * @param inputPort The port to use for communication
	 * @throws IOException
	 */
	private void initialiseServer(int inputPort) throws IOException {
		while (true) {
		try {
		serverSocket = new ServerSocket(inputPort);
		} catch (IOException e) {
		System.err.println("The port is Unavailable.");
		e.printStackTrace();
		}
		Socket socket = null;
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Port already Closed");
			e.printStackTrace();
		}
		System.out.println("Started server....");
		request = readRequest(socket);
		System.out.println(request);
		processClientRequest(socket);

		try {
			socket.close();
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("Port already Closed");
			e.printStackTrace();
		}
		}
}
	/**
	 * Recieves the client request and processes the request by breaking it down into parts.
	 * @param s The active socket that receives communication from client
	 * @throws IOException
	 */
	private void processClientRequest(Socket s) throws IOException {
		String responseCode = null;
		String resourceURL = null;
		String contentType = null;
		String httpVersion = "HTTP/1.1"; //default value if version is not provided
		File resourceLocation = null;
		StringTokenizer strtok = new StringTokenizer(request);

		if (strtok.countTokens() != 3) {
			status = "400 Bad Request";
			resourceLocation = new File(webDirectory + "/400.html");
			contentType = "text/html";
		}
		else {
			responseCode = strtok.nextToken();
			resourceURL = strtok.nextToken();
			resourceURL = resourceURL.substring(1);
			httpVersion = strtok.nextToken();
			if (resourceURL.equals("")) {
				resourceURL = "index.html";
			}
			resourceLocation = new File(webDirectory + "/" + resourceURL);

			if (checkMethodType(responseCode)) {

				if (resourceLocation.exists()) {
					status = "200 OK";
					contentType = getContentType(resourceURL);
				}
				else {
					status = "404 Not Found";
					resourceLocation = new File(webDirectory + "/404.html");
					contentType = getContentType(resourceURL);
				}

			}
			else {
				status = "501 Not Implemented";
				resourceLocation = new File(webDirectory + "/501.html");
				contentType = getContentType(resourceURL);
			}
		}
		sendResponse(s, status, httpVersion, responseCode, contentType, resourceLocation);
		//Writing logs
		PrintWriter pw1 = new PrintWriter(new FileOutputStream("log.txt", true));
		pw1.append("\nServer Logs: \nLog created on " + new Date()
				+ "\nResponse Code: " + responseCode + "\nRequested Resource: " + resourceLocation + "\nStatus: " + status + "\n");
		pw1.flush();
		pw1.close();
	}

	/**
	 * Takes in the resource request and returns the type of content requested.
	 * @param resourceURL the URL of the requested content
	 * @return The type of content requested
	 */
	private String getContentType(String resourceURL) {
		String extension = resourceURL.substring(resourceURL.lastIndexOf(".") + 1);
		String contentType = null;
		extension = extension.toLowerCase();
		if (extension.equals("html") || extension.equals("htm") || extension.equals("txt")) {
			contentType = "text/html";
		}
		else if (extension.equals("jpg") || extension.equals("jpeg")) {
			contentType = "image/jpeg";
		}
		else if (extension.equals("png")) {
			contentType = "image/png";
		}
		else if (extension.equals("gif")) {
			contentType = "image/gif";
		}
		return contentType;
	}

	/**
	 * this receives the request from client.
	 * @param s The active socket that receives communication from client
	 * @return The request received from client
	 * @throws IOException
	 */
	private String readRequest(Socket s) throws IOException {
		instr = s.getInputStream();
		br = new BufferedReader(new InputStreamReader(instr));
		return br.readLine();
	}
	/**
	 * validates if the requested response code is implemented or not.
	 * @param methodType the requested response code part of the client message
	 * @return Response code is valid or not
	 */
	private boolean checkMethodType(String methodType) {
		return (methodType.equals("GET") || methodType.equals("HEAD"));
	}
	/**
	 * @param s The active socket that receives communication from client.
	 * @param status The current status of the request
	 * @param httpVersion The version of the request
	 * @param responseCode The requested response code from client
	 * @param contentType The requested content type from client
	 * @param file the content requested by the client
	 * @throws IOException
	 */
	private void sendResponse(Socket s, String status, String httpVersion, String responseCode, String contentType, File file) throws IOException {
		String serverDetails = "Server: Simple Java Http Server";
		outstr = s.getOutputStream();
		pw = new PrintWriter(outstr);
		brout = new BufferedOutputStream(outstr);
		int fileLength = (int) file.length();

			pw.println(httpVersion + " " + status);
			pw.println(serverDetails);
			pw.println("Content-Type: " + contentType);
			pw.println("Content-Length: " + fileLength);
			pw.println();
			pw.flush();
		if (responseCode.equals("GET")) {
			FileInputStream fileInStr = new FileInputStream(file);
			byte[] fileBytes = new byte[fileLength];
			fileInStr.read(fileBytes);
			fileInStr.close();
			brout.write(fileBytes, 0, fileLength);
			brout.flush();
		}
	}
}

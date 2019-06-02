import java.io.File;
import java.io.IOException;

/**
 * validates the port and working web directory and initialises an object for ServerImplementation class.
 * @author 180029539
 *
 */
public class WebServerMain {
         /**
	 * If arguments are valid, initialises an object for ServerImplementation class.
	 * @param args Working Web Directory, Port to Listen on
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		int inputPort;
		if (args.length == 2) {

		if (checkArguments(args[0], args[1])) {

			inputPort = Integer.parseInt(args[1]);
			try {
			ServerImplementation s1 = new ServerImplementation(args[0], inputPort);
			Thread thread = new Thread(s1);
			thread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		}
		else {
			System.out.println("Usage: java WebServerMain <document_root> <port>");
		}
	}

	/**
	 * validates the input arguments of web directory and the port entered.
	 * @param webDirectory Working Web Directory
	 * @param port Port to Listen on
	 * @return whether arguments are valid or not
	 */
	private static boolean checkArguments(String webDirectory, String port) {

		try {
			int inputPort = Integer.parseInt(port);
			return (checkPort(inputPort)	&&	checkDirectory(webDirectory));
		}
		catch (NumberFormatException ne) {
			System.err.println("Port is Invalid");
			ne.printStackTrace();
		}
		return false;
	}

	/**
	 * Validate the port number entered.
	 * @param port Port to Listen on
	 * @return valid port or not
	 */
	private static boolean checkPort(int port) {
		return (port > 1023	&&	port < 65535);
	}

	/**
	 * validate the web directory entered.
	 * @param webDirectory Working Web Directory
	 * @return valid web directory or not
	 */
	private static boolean checkDirectory(String webDirectory) {
		File currentDirectory = new File(webDirectory);
		if (currentDirectory.isDirectory()) {
			if (currentDirectory.list().length > 0) {
				return true;
			}
			else {
				System.err.println("Provided Web Directory is empty");
				return false;
			}
		}
		else {
			System.err.println("Provided Web Directory is not a Directory");
			return false;
		}
	}
}

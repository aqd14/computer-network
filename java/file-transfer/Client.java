import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Usage: java Client <host/server address> <port-number> <filename>
 */

/**
 * A client class for sending files to server. Note that the file formatting
 * must be reserved! This is two-phase process. First, the client negotiates the
 * available port number in which server has it available. Next, sending file
 * over established connection.
 * 
 * @author aqd14
 */
public class Client {
	// Attributes
//	private Socket socket; 		// Socket on the client side to send data through transport layer.
	PrintWriter err = new PrintWriter(new OutputStreamWriter(System.err), true); 	// Print error messages to screen
	PrintWriter ack = new PrintWriter(new OutputStreamWriter(System.out), true);  // Print acknowledge message to screen
	
	// The character represents when client want to negotiate a port number with server
	// Must be agreed from both sides
	final char NEGOTIATION_CHAR = 259;
	
	// Packet's size
	final static int PACKET_SIZE = 4; 

	/**
	 * Constructor
	 */
	public Client(String host, String port, String pathname) {
		// Validate input beforehand
		if (validateInput(host, port, pathname) == false) {
			err.println("Invalid input... Terminate program! Please try again.");
			return;
		}
		
		int serverPort = negotiate(host, port);
		// Something went wrong. Server can't return a port number:
		if (serverPort == -1) {
			return;
		}
		sendFile(host, serverPort, pathname);
	}
	
	/**
	 * Setup TCP connection with server to agree on port number.
	 * 
	 * @param host	server host
	 * @param port	server's initial port number
	 * @return server's running port number
	 */
	public int negotiate(String host, int port) {
		int responsePort = -1;
		try {
			Socket socket = new Socket(host, port);
			
			// Establish data input/output streams
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			
			// Send port negotiation request to server
			out.writeChar(NEGOTIATION_CHAR);
			// Ready to receive response (port number) from server
			responsePort = in.readInt();
			// Negotiation stage finished. Close connection!
			socket.close();
		} catch (UnknownHostException e) {
			err.println("IP address of the host couldn't be resolved!");
		} catch (IOException e) {
			err.println("An I/O error occurs when creating the socket. Probably server has not started yet?");
		}
		return responsePort;
	}
	
	public int negotiate(String host, String port) {
		return negotiate(host, Integer.parseInt(port));
	}
	
	/**
	 * Send file through socket with UDP connection.
	 * 
	 * @param host
	 * @param port
	 * @param pathname
	 * @throws IOException
	 */
	public void sendFile(String host, int port, String pathname) {
		// Now, ready to send file to server
	    try {
    		DatagramSocket socket = new DatagramSocket();
    		// Server address
    		InetAddress address = InetAddress.getByName(host);
    		// Sending file to server
    		DatagramPacket sentPacket, receivedPacket;
    		FileInputStream fis = new FileInputStream(new File(pathname));
    		byte[] sentBuf = new byte[PACKET_SIZE]; // Send 4 characters per packet
    		byte[] receivedBuf = new byte[PACKET_SIZE]; // Receive packet buffer from socket
    		int offset; // Number of bytes read successfully
    		while ((offset = fis.read(sentBuf)) != -1) {
    			sentPacket = new DatagramPacket(sentBuf, offset, address, port);
    			socket.send(sentPacket);
    			// Completed sending packet. Waiting to get response from server
    			receivedPacket = new DatagramPacket(receivedBuf, receivedBuf.length);
    			socket.receive(receivedPacket);
    			// Print out response from server
    			ack.println(new String(receivedPacket.getData(), 0, receivedPacket.getLength()));
    		}
    		
            // Completed sending file. Close socket and streams will then be closed automatically
            fis.close();
            socket.close();
	    } catch (IOException e) {
	        err.println("IOException occurred. Close connection!");
	    }
	}
	
	private boolean validateInput(String host, String port, String fileName) {
		return validateHost(host) && validatePort(port) && validateFile(fileName);
	}
	
	private boolean validateHost(String host) {
		return true;
	}
	
	/**
	 * Port must be parsable to {@link Integer} and its value must be in range 1024 - 65535 (inclusively)
	 * 
	 * @param port the port to check
	 * @return {@code true} if the port is valid. Otherwise return {@code false}
	 */
	private boolean validatePort(String port) {
		boolean isValid = false;
		try {
			int value = Integer.parseInt(port);
			// Port is out of range
			if (value < 1024 || value > 65535) {
				isValid = false;
				err.println("Port must be in range 1024 - 65535");
			}
			isValid = true;
		} catch (NumberFormatException e) { // Parse error
			isValid = false;
			err.println("Port must be integer: " + port);
		}
		return isValid;
	}
	
	/**
	 * Check if file exist or not.
	 * 
	 * @param fileName file path to check
	 * @return {@code true} if file exists
	 */
	private boolean validateFile(String fileName) {
		File f = new File(fileName);
		boolean isValid = f.exists();
		if (!isValid) {
			err.println("File is not exist! Check your filepath.");
		}
		return isValid;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		if (args == null || args.length < 3) {
			System.err.println("Need to provide host, port number and filename to start!\nUsage: java client <hostname> <port> <filepath>");
			return; // Terminate program when input is invalid
		}
		// Start connecting
		new Client(args[0], args[1], args[2]);
	}
}

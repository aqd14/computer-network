import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Usage: java Server <port-number>
 */

/**
 * <p>
 * An implementation of server side. Be ready to receive files sent from
 * clients. This is 3-way handshaking process where client and server first
 * agree on a common channel.
 * </p>
 * 
 * <p>
 * Server after receive opening request from client will generate a random port
 * number from 1024 to 65535 and send back to the client. An UDP connection will
 * be established using this port. The actual file will be sent through UDP
 * connection when buffer's size is 4.
 * </p>
 * 
 * @author aqd14
 */
public class Server extends Thread {
	// Attributes
//	private ServerSocket ss; // Server socket
	PrintWriter err = new PrintWriter(new OutputStreamWriter(System.err), true); 	// Print error messages to screen
	PrintWriter ack = new PrintWriter(new OutputStreamWriter(System.out), true);  // Print acknowledge message to screen
	
	private int initialPort;
	private int port;
	
	// Packet's size
    final static int PACKET_SIZE = 4;
	
	// Default file name received from client
	// Received file will be written at the current working directory
	final String DEFAULT_OUTPUT = "output.txt";
	
	// The character represents when client want to negotiate a port number with server
	// Must be agreed from both sides
	final char NEGOTIATION_CHAR = 259;
	
	// Range of possible generated ports from server side
	final int from = 1024; 
	final int to = 65535;
	
	/**
	 * Constructor
	 */
	public Server(String initialPort) {
		// Validate port
		if (validatePort(initialPort) == false) {
			err.println("Invalid port ... Terminate program! Try again.");
			return;
		}
		this.initialPort = Integer.parseInt(initialPort);
	}
	
	/**
	 * Negotiate port number with connecting client over TCP connection
	 * 
	 * @throws IOException
	 */
	private void negotiate() {
	    try {
    		ServerSocket ss = new ServerSocket(initialPort);
    //		ss.setReuseAddress(true);
    //		ss.bind(new InetSocketAddress("localhost", Integer.parseInt(port)));
    		
    		Socket socket = ss.accept();
    		// Establish data input/output streams
    		DataInputStream dis = new DataInputStream(socket.getInputStream());
    		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    		
    		char c = dis.readChar();
    		// Client want to negotiate a port number
    		// Generate a random port within the defined range then send back to client
    		if (c == NEGOTIATION_CHAR) {
    			this.port = generateRandomNumer(from, to);
    			out.writeInt(this.port);
    			ack.println("Negotiation detected. Please select the random port " + this.port);
    			// Close connection
    			ss.close();
    		} else {
    			err.println("Negotiation char is not matched: " + c + " != " + NEGOTIATION_CHAR);
    		}
	    } catch (IOException e) {
	        err.println(e.getStackTrace());
	    }
	}
	
	public void run() {
	   negotiate();
	   receiveFile();
	}
	
	/**
	 * Establish a UDP connection to client with agreed port number.
	 * Receive 4-byte packets from client and write received contents to file.
	 * Send back the received content to client with upper-case representation.
	 */
	private void receiveFile() {
        try {
            // Server's socket;
            DatagramSocket ds = new DatagramSocket(port);
            // Ready to receive packets from client
            BufferedWriter bw = new BufferedWriter(new FileWriter(DEFAULT_OUTPUT));
            int length = 0;
            do {
                byte[] buffer = new byte[PACKET_SIZE]; // Each packet contains 4 characters. 1 byte per character
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                ds.receive(packet);
                
                // Might not be equal to buffer's length (i.e, some last remaining characters)
                length = packet.getLength();
                // Write received packet to file on the server side
                // The actual received data might not equal to whole buffer capacity
                bw.write(new String(packet.getData(), 0, length));
                
                // Convert received characters to upper-case,
                // decode to bytes then send back to client
                buffer = generateResponse(buffer).getBytes();
                InetAddress clientAdd = packet.getAddress();
                int clientPort = packet.getPort();
                packet = new DatagramPacket(buffer, length, clientAdd, clientPort);
                ds.send(packet);
            } while (length == PACKET_SIZE);
            // Close connection
            bw.close();
            ds.close();
        } catch (IOException e) {
            err.println("IOException occurred when creating socket!");
        }
	}
	
	/**
	 * Convert received bytes data to the upper-case string representation
	 * @param data
	 * @return
	 */
	private String generateResponse(byte[] data) {
	    return new String(data).toUpperCase();
	}
	
	/**
	 * Randomly generate a random number within a given range.
	 * 
	 * @param from	starting number
	 * @param to	ending number
	 * @return 		a random number within range
	 */
	private int generateRandomNumer(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	/**
	 * Port must be parsable to {@link Integer} and its value must be in range 1024 - 65535 (inclusively)
	 * 
	 * @param port the port to check
	 * @return {@code true} if the port is valid. Otherwise return {@code false}
	 */
	private boolean validatePort(String port) {
		boolean isValid = true;
		try {
			int value = Integer.parseInt(port);
			// Port is out of range
			if (value < from || value > to) {
				isValid = false;
				err.println("Port must be in range 1024 - 65535");
			}
		} catch (NumberFormatException e) { // Parse error
			isValid = false;
			err.println("Port must be integer. Received: " + port);
		}
		return isValid;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length != 1) {
			throw new RuntimeException("Must specify only port number to start server!");
		}
		// Start server
		new Server(args[0]).start();
	}
}

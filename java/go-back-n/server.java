import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author aqd14
 *
 */
public class server {
    // Attributes
    private DatagramSocket socket;
    // host address of the emulator
    private InetAddress emulatorAdd;
    // UDP port number used by emulator to receive data from client
    private int emulatorSendingPort;
    // UDP port number used by client to receive ACKs from emulator
    private int serverReceivingPort;
    
    private PrintWriter arrivalLogger;
    /**
     * 
     * @param hostname
     *            Host address of the emulator
     * @param receivingPort
     *            Emulator's port where server receives {@link DatagramPacket} from
     * @param sendingPort
     *            Emulator's port where server sends packet through
     * @param filepath
     *            A file that want to write received contents to
     */
    public server(String hostname, int receivingPort, int sendingPort, String filepath) {
        // Initialize attributes
        emulatorSendingPort = sendingPort;
        serverReceivingPort = receivingPort;
        try {
            arrivalLogger = new PrintWriter(new FileWriter("arrival.log", false), true);
            emulatorAdd = InetAddress.getByName(hostname);
            socket = new DatagramSocket(serverReceivingPort);
            receiveFile(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendPacket(packet p) {
        DatagramPacket packet = regulator.makeDatagramPacket(p, emulatorAdd, emulatorSendingPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void receiveFile(String filepath) throws IOException {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(filepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        
        int seqNum;
        int expectedSeqNum = 0;
        byte[] buf = new byte[regulator.WHOLE_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        
        while(true) {
            socket.receive(packet);
            packet p = regulator.extractDatagramPacket(packet);
            seqNum = p.getSeqNum();
            arrivalLogger.println(seqNum);
            if (p.getType() != regulator.EOT_CTS) {
                if (seqNum == expectedSeqNum) {
                    System.out.println("----------------------");
                    System.out.println("Received correct packet: " + seqNum);
                    // Write received content to file
                    writer.write(p.getData());
                    
                    // Send ack packet back to client
                    packet ack = regulator.makePacket(regulator.ACK, seqNum, null, 0);
                    sendPacket(ack);
                    
                    expectedSeqNum++;
                    if (expectedSeqNum >= regulator.MAX_SEQUENCE_NUMBER) {
                        expectedSeqNum = 0;
                    }
                } else {
                    // Just discard packet
                    System.out.println("Received packet out-of-order. Expected: " + expectedSeqNum + " ---- Received: " + seqNum);
                    System.out.println("Discard packet: " + seqNum);
                    p = null;
                    // and resend the most recently received packet
                    // TODO: What if the first packet is lost?
                    packet ack = regulator.makePacket(regulator.ACK, expectedSeqNum-1, null, 0);
                    sendPacket(ack);
                    
                    System.out.println("Resend last in-order packet: " + (expectedSeqNum-1));
                    System.out.println("--------------------------\n\n");
                }
            } else {
                // Send ack packet back to client
                System.out.println("-----------------\nReceived End-of-Trasmission packet from client. Send feedback to client: " + seqNum);
                packet endPack = regulator.makePacket(regulator.EOT_STC, seqNum, null, -1);
                sendPacket(endPack);
                writer.close();
                socket.close();
                System.out.println("Closed server socket!\n----------------");
                return;
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("The program should have exactly 3 arguments!");
            return;
        }
        
        String host = args[0];
        int receivingPort = Integer.parseInt(args[1]);
        int sendingPort = Integer.parseInt(args[2]);
        String filepath = args[3];
        
        new server(host, receivingPort, sendingPort, filepath);
    }

}

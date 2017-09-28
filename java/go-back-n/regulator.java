
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Define a list of shared variables or attributes that will be used by both
 * client and server
 * 
 * @author doquocanh-macbook
 */
public class regulator {
    // Final variables

    public static final int WINDOW_SIZE = 7; // Window size for Go-Back-N
                                             // protocol,
    // which is the maximum number of
    // outstanding-but-not-acknowledged
    // packets
    public static final int TIMEOUT = 2000; // Timeout (in miliseconds) for the oldest
                                            // sent-but-not-acknowledged-yet pack

    public static final int MAX_DATA_LENGTH = 30; // Maximum payload data
    public static final int MAX_SEQUENCE_NUMBER = 8;
    
    public static final int WHOLE_PACKET_SIZE = 1024; // Size of serialized packet object
    
    // Packet type
    public static final int ACK = 0;
    public static final int DATA_PACKET = 1;
    public static final int EOT_STC = 2; // End-of-transmission signal from
                                         // server to client
    public static final int EOT_CTS = 3; // End-of-transmission signal from
                                         // client to server
    
    /**
     * Create a datagram packet.
     * 
     * @param packetType    packet type
     * @param seqNum        sequence number
     * @param buf           buffer data
     * @param offset        offset of buffer
     * 
     * @return  null if invalid input or exception occurred. Otherwise returns new DatagramPacket instance with given data
     */
    public static DatagramPacket makeDatagramPacket(packet p, InetAddress address, int port) {
        DatagramPacket packet = null;
        try {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ObjectOutputStream objectOS = new ObjectOutputStream(byteArrayOS);
            objectOS.writeObject(p);
            
            // Make packet
            byte[] buffer = byteArrayOS.toByteArray();
            packet = new DatagramPacket(buffer, 0, buffer.length, address, port);
            
            // Close resources
            objectOS.close();
            byteArrayOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet;
    }
    
    /**
     * Make {@link packet} instance from buffer
     * 
     * @param packetType    packet type
     * @param seqNum        sequence number
     * @param buf           buffer data
     * @param offset        how many data is available in buffer
     * @return a {@link packet} instance. Or return {@code null} if the packet type is invalid
     */
    public static packet makePacket(int packetType, int seqNum, byte[] buf, int offset) {
     // determine payload length and payload data given packet type
        int length = 0;
        String payload = null;
        switch (packetType) {
            case ACK:   // use default length and payload
            case EOT_STC:
            case EOT_CTS:
                break;
            case DATA_PACKET:
                length = offset;
                payload = new String(buf, 0, offset);
                break;
            default:
                // invalid packet type
                return null;
        }
        packet p = new packet(packetType, seqNum, length, payload);
        return p;
    }
    
    /**
     * Deserialize {@link java.net.DatagramPacket} instance to {@link packet} instance 
     * 
     * @param packet a {@link java.net.DatagramPacket} to be deserialized
     * 
     * @return null if exceptions occurred. Otherwise, returns a deserialized {@link packet} instance
     */
    public static packet extractDatagramPacket(DatagramPacket packet) {
        packet p = null;
        ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
        try {
            ObjectInputStream objectIS = new ObjectInputStream(byteArrayIS);
            p = (packet) objectIS.readObject();
            
            // Close resources
            objectIS.close();
            byteArrayIS.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return p;
    }
}

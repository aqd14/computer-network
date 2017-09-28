

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Usage: java Client <host/server address> <port-number> <filename>
 */

/**
 * A client class for sending files to server by using UDP. A window of size 7
 * would be used to transfer packets using Go-Back-N protocol.
 * 
 * @author aqd14
 */
public class client {
    // Class attributes
    private PrintWriter seqNumLogger; // Sequence number logger
    private PrintWriter ackLogger; // ACK logger

    private PrintWriter errLogger = new PrintWriter(System.err);
    
    private ArrayList<packet> packets;
    
    private DatagramSocket socket;
    // host address of the emulator
    private InetAddress emulatorAdd;
    // UDP port number used by emulator to receive data from client
    private int emulatorSendingPort;
    // UDP port number used by client to receive ACKs from emulator
    private int clientReceivingPort;
    
    /**
     * 
     * @param hostname
     *            Host address of the emulator
     * @param sendingPort
     *            Emulator's port where client sends packet through
     * @param receivingPort
     *            Emulator's port where client receives ACKs from
     * @param filepath
     *            A file that client wants to send to server
     */
    public client(String hostname, int sendingPort, int receivingPort, String filepath) {
        // Initialize port numbers
        this.emulatorSendingPort = sendingPort;
        this.clientReceivingPort = receivingPort;
        
        packets = new ArrayList<>();
        preparePackets(filepath);
        
        // Can't make packets. Terminate program
        if (packets == null || packets.size() == 0) {
            return;
        }
        
        try {
            seqNumLogger = new PrintWriter(new FileWriter("seqnum.log", false), true);
            ackLogger = new PrintWriter(new FileWriter("ack.log", false), true);
            
            emulatorAdd = InetAddress.getByName(hostname);
            
            // Create a socket on the client side and bind the specific port
            // number, which emulator will use to send ack back to client
            socket = new DatagramSocket(clientReceivingPort);
            
            readAndTransferFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendPacket(packet p) {
        try {
            DatagramPacket packet = regulator.makeDatagramPacket(p, emulatorAdd, emulatorSendingPort);
            socket.send(packet);
            System.out.println("Send packet: " + p.getSeqNum());
            seqNumLogger.println(p.getSeqNum());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public packet receivePacket() throws SocketTimeoutException {
        byte[] buf = new byte[regulator.WHOLE_PACKET_SIZE];
        DatagramPacket ack = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(ack);
            packet p = regulator.extractDatagramPacket(ack);
            return p;
        } catch (SocketTimeoutException e) {
//            e.printStackTrace();
            throw new SocketTimeoutException();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Read content from file and transfer through network using UDP
     * @param hostname
     * @param port
     * @param filepath
     */
    private void readAndTransferFile() {
        try {
            int baseSeq = 0; // the sequence number of the oldest unacknowledged packet
            
            int packetNum = 0; // the index of current sending packet in packet list
            
            int nextSeq = 0; // the smallest unused sequence number (that is,
                             // the sequence number of the next packet to be sent)
                             // value in range of [0, MAX_SEQUENCE_NUMBER)
            
//            int lastAckNum = 0;
            
            boolean reachedEnd = false;
            
            // Continuously transfer datagram packet when there are available content in file
            // keep track of current base sequence number, current sending sequence number and the next sequence number
            // the transferring process takes place from sequence number 0,
            while (true) {
                System.out.println("----------------------");
                // If the window is not full and there are some data left, send
                // all the available packets within sliding window
                while (packetNum < baseSeq + regulator.WINDOW_SIZE && packetNum < packets.size()) {
                    sendPacket(packets.get(packetNum));
                    // Ready for the next sending event
                    if (baseSeq == packetNum) {
                        // Start timer
//                        System.out.println("Set timeout!");
                        socket.setSoTimeout(regulator.TIMEOUT);
                    }
                    
//                    System.out.println("Base sequence number: " + baseSeq);
//                    System.out.println("Next sequence number : " + nextSeq);
//                    System.out.println("Packet number: " + packetNum);
                    
                    packetNum++;
                    nextSeq++;
                    
                    // Reset next sequence number of reached maximum
                    if (nextSeq >= regulator.MAX_SEQUENCE_NUMBER) {
                        nextSeq = 0;
                    }
                }
                
                // Check if any ACK packet sending from server
                while (true) {
                    try {
                     // Received datagram packet from server
                        packet ackPack = receivePacket();
                        if (ackPack == null) {
                            errLogger.println("Exception occurred!");
                            continue;
                        }
                        
                        int ackSeqNum = ackPack.getSeqNum();
                        System.out.println("Received ACK: " + ackSeqNum);
                        ackLogger.println(ackSeqNum);
                        int recPackType = ackPack.getType();
                        if (recPackType == regulator.ACK) {
                            System.out.println("base sequence number before: " + baseSeq);
                            baseSeq = updateBaseSeq(baseSeq, packetNum, ackSeqNum); // Update cumulative ACKs
                            System.out.println("base sequence number after: " + baseSeq);
//                            lastAckNum = ackPack.getSeqNum();
                            if (baseSeq == packetNum) {   // Stop timer when there is no outstanding packet
                                socket.setSoTimeout(0); // Infinite timeout
                            } else {
                                // Start timer
                                socket.setSoTimeout(regulator.TIMEOUT);
                            }
                            
                            if (packetNum == packets.size() && nextSeq == ackSeqNum+1) {
                                reachedEnd = true;
                            }
                        } else if (recPackType == regulator.EOT_STC){ // server requests to end transmission
                            socket.close();
                            return;
                        }
                        // No parallel operations required so just break out the
                        // loop when received ack packet from server
                        break;
                    } catch (SocketTimeoutException e) { // timeout
                        // set timer
                        socket.setSoTimeout(regulator.TIMEOUT);
                        // resend all the previously sent packets upto next sequence number
                        // TODO: Need to update sequence number when resend packet?
                        int from  = baseSeq;
                        int to = packetNum;
                        System.out.println("Timeout occurred! Sending back previous packets from " + from + " to " + (to-1));
                        for (int i = from; i < to; i++) {
                            packet p = packets.get(i);
                            sendPacket(p);
                        }
                        break;
                    }
                }
                
                // No available packet to send
                // Received the last ack
                // Now, send End-of-Transmission signal to server
                if (reachedEnd) {
                    System.out.println("Sending the End-of-Transmission packet: " + nextSeq);
                    packet endPack = new packet(regulator.EOT_CTS, nextSeq, 0, null);
                    sendPacket(endPack);
                }
                
                System.out.println("---------------------\n\n");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * <p>
     * Construct packets from file content. Read file and make all the packets.
     * Each packet should contain 30 bytes as payload whenever there are
     * contents to read from file. The last packet might not have full 30 bytes
     * as the payload, though.
     * </p>
     * 
     * @param filepath
     *            file to send
     */
    private void preparePackets(String filepath) {
        try (FileInputStream reader = new FileInputStream(filepath)) {
            int offset;
            byte[] buf = new byte[regulator.MAX_DATA_LENGTH];
            int seqNum = 0; // Temporarily set sequence number to -1 for all
                             // packets. Need to update when actually
                             // transferring data
            while ((offset = reader.read(buf)) != -1) {
                if (seqNum >= regulator.MAX_SEQUENCE_NUMBER) {
                    seqNum = 0;
                }
                packet p = regulator.makePacket(regulator.DATA_PACKET, seqNum, buf, offset);
                packets.add(p);
                seqNum++;
            }
        } catch (FileNotFoundException e) {
            errLogger.println("File [" + filepath + "] not found!");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update base sequence number based on most recently received ack packet.
     * The base sequence number should reflect the position in the packet list
     * 
     * @param baseSeq index of oldest unacknowledged packet in the packets list
     * @param seqNum
     * @return  updated base sequence number
     */
    private int updateBaseSeq(int baseSeq, int nextSeq, int seqNum) {
        System.out.println("[Update baseseq] nexSeq = " + nextSeq);
        System.out.println("[Update baseseq] ackseq = " + seqNum);
        
        /**
        int temp = -1;
        if (seqNum == -1) {
            // the packet with sequence number [0] is lost. Keep current base sequence
            return baseSeq;
        } else if (seqNum == baseSeq) {
            temp = 0;
        } else if (seqNum > baseSeq % regulator.MAX_SEQUENCE_NUMBER) {
            // restart new sequence number
            temp = seqNum - (baseSeq % regulator.MAX_SEQUENCE_NUMBER);
        } else {
            temp = regulator.MAX_SEQUENCE_NUMBER - (baseSeq % regulator.MAX_SEQUENCE_NUMBER) + seqNum;
        }
        
        System.out.println("Temp = " + temp);
        return baseSeq+temp+1 > nextSeq ? baseSeq : baseSeq+temp+1;
        **/
        
        int remainder = baseSeq % regulator.MAX_SEQUENCE_NUMBER; 
        
        if (seqNum > remainder) {
            baseSeq = baseSeq + (seqNum - remainder);
        } else if (seqNum < remainder) {
            // There are two cases:
            //      1. The last received packet was sent again
            //      2. The packet in the next iteration
            // Need to consider window size to determine
            // Assume we receive 0 in second attempt, because it
            // is out of sliding window so it falls into 1st case.
            // So we keep base sequence number
            
            /**
             * 
            next    0   1   2   3   4   5   6   7   8   9   10  11

            base    0   1   2   3   4   5   6   7   8   9   10  11

            ack     0   1   2   3   4   5   6   7   0   1   2   3

                    |   |   |   |   |   |   |

                        |   |   |   |   |   |   |
                        
            **/
            
            if ((baseSeq + regulator.WINDOW_SIZE) % regulator.MAX_SEQUENCE_NUMBER >= seqNum) {
                return baseSeq;
            } else {
                baseSeq = baseSeq + (regulator.MAX_SEQUENCE_NUMBER - baseSeq % regulator.MAX_SEQUENCE_NUMBER + seqNum);
            }
        } else {
            // baseSeq++;
        }
        return baseSeq+1;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        args = new String[] {"localhost", "6000", "6001", "file.txt"};
        if (args.length != 4) {
            System.err.println("The program should have exactly 4 arguments!");
            return;
        }
        
        String host = args[0];
        int receivingPort = Integer.parseInt(args[1]);
        int sendingPort = Integer.parseInt(args[2]);
        String filepath = args[3];
        
        new client(host, receivingPort, sendingPort, filepath);
    }

}

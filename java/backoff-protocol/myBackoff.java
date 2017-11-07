import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Anh Do
 *
 */
public class myBackoff {
    final int STARTING_SLOT = 1;
    final int INITIAL_WINDOW_SIZE_LINEAR_BACKOFF = 2;
    final int INITIAL_WINDOW_SIZE_BINARY_EXPONENTIAL_BACKOFF = 2;
    final int INITIAL_WINDOW_SIZE_TRINARY_EXPONENTIAL_BACKOFF = 3;
    final int INITIAL_WINDOW_SIZE_LOGLOG_BACKOFF = 3;
    
    final static int DEFAULT_NUM_DEVICES = 6000;
    
    private int numDevices;
    
    public myBackoff() {
        this(DEFAULT_NUM_DEVICES);
    }
    
    /**
     * 
     */
    public myBackoff(int numDevices) {
        this.numDevices = numDevices;
    }
    
    public void runSimulation() throws IOException {
        runSimulation(numDevices);
    }
    
    public void runSimulation(int numWindows) throws IOException {
        // Linear backoff protocol simulation
        PrintWriter linearBackoffWriter = new PrintWriter(new FileWriter("linearLatency.txt", false), true);
        LinearBackoff linearBackoffProtocol = new LinearBackoff();
        transmit(linearBackoffProtocol, linearBackoffWriter);
        
        // Binary backoff protocol simulation
        PrintWriter binaryExpBackoffWriter = new PrintWriter(new FileWriter("binaryLatency.txt", false), true);
        BinaryExponentialBackoff binaryExpBackoffProtocol = new BinaryExponentialBackoff();
        transmit(binaryExpBackoffProtocol, binaryExpBackoffWriter);
        
        // Trinary backoff protocol simulation
        PrintWriter trinaryExpBackoffWriter = new PrintWriter(new FileWriter("trinaryLatency.txt", false), true);
        TrinaryExponentialBackoff trinaryExpBackoffProtocol = new TrinaryExponentialBackoff();
        transmit(trinaryExpBackoffProtocol, trinaryExpBackoffWriter);
        
        // Log log backoff protocol simulation
        PrintWriter loglogBackoffWriter = new PrintWriter(new FileWriter("loglogLatency.txt", false), true);
        LogLogBackoff loglogBackoffProtocol = new LogLogBackoff();
        transmit(loglogBackoffProtocol, loglogBackoffWriter);
    }
    
    /**
     * Transmit data with backoff protocol
     * @param bp
     * @param writer
     */
    public void transmit(IBackoffProtocol bp, PrintWriter writer) {
        System.out.println("----- START SIMULATING TRANSMITION WITH BACKOFF PROTOCOL -----\n");
        final int MAXIMUM_REPEATITION_FOR_EACH_SIMULATION = 10;
        int maximumTestingDevicesForCurrentTrial = 100;
        int repeatition = 1;
        long totalLatency = 0;
        
        int initialWindowSize = 0;
        if (bp instanceof LinearBackoff) {
            initialWindowSize = INITIAL_WINDOW_SIZE_LINEAR_BACKOFF;
            System.out.println("---- LINEAR BACKOFF PROTOCOL ----\n");
        } else if (bp instanceof BinaryExponentialBackoff) {
            initialWindowSize = INITIAL_WINDOW_SIZE_BINARY_EXPONENTIAL_BACKOFF;
            System.out.println("---- BINARY EXPONENTIAL BACKOFF PROTOCOL ----\n");
        } else if (bp instanceof TrinaryExponentialBackoff) {
            initialWindowSize = INITIAL_WINDOW_SIZE_TRINARY_EXPONENTIAL_BACKOFF;
            System.out.println("---- TRINARY EXPONENTIAL BACKOFF PROTOCOL ----\n");
        } else if (bp instanceof LogLogBackoff) {
            initialWindowSize = INITIAL_WINDOW_SIZE_LOGLOG_BACKOFF;
            System.out.println("---- LOG LOG BACKOFF PROTOCOL ----\n");
        } else {
            assert false;
        }
        
        while(true) {
            Window initialWindow = new Window(initialWindowSize);
            Window curWindow = initialWindow;
            int desiredSlot; // The slot that current machine transmits
            
//            System.out.println("Maximum testing device = " + maximumTestingDevicesForCurrentTrial);
            for (int device = 1; device <= maximumTestingDevicesForCurrentTrial;) {
                desiredSlot = curWindow.getRandomSlotWithinWindow();
                if (curWindow.isSlotTaken(desiredSlot)) { // Collision occurred
                    totalLatency += curWindow.getNumSlots();
                    curWindow = bp.nextWindow(curWindow);
                } else { // Slot is available. Next device can start transmitting
                    curWindow.markSlotTaken(desiredSlot);
                    device++;
                }
                // Finish a simulation
                // Start next trial
                if (device > maximumTestingDevicesForCurrentTrial) {
//                    System.out.println("Repeatition = " + repeatition);
//                    System.out.println("Number of device = " + device);
                    if (repeatition < MAXIMUM_REPEATITION_FOR_EACH_SIMULATION) {
                        totalLatency += desiredSlot;
                        repeatition++;
                    } else {
//                        System.out.println("[Repetition " + repeatition + "]" + " Total latency = " + totalLatency);
                        writer.println((double) totalLatency / MAXIMUM_REPEATITION_FOR_EACH_SIMULATION);
                        // Prepare for next trial
                        maximumTestingDevicesForCurrentTrial += 100;
                        repeatition = 1;
                        totalLatency = 0;
                        if (maximumTestingDevicesForCurrentTrial > numDevices) {
                            System.out.println("----- ENDING SIMULATION ----- \n");
                            System.out.println("--------------------------------- \n");
                            return;
                        }
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        myBackoff backoff = new myBackoff();
        backoff.runSimulation();
    }
}

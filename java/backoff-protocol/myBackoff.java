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
        
        Window curWindow = new Window(initialWindowSize);
        int device = 1;
        int totalSentDevices = 0;
        while (true) {
        	for (; device <= maximumTestingDevicesForCurrentTrial; device++) {
        		curWindow.takeRandomSlotWithinWindow();
        	}
        	
        	int succesfullySentDevices = curWindow.getEligibleSlots();
        	totalSentDevices += succesfullySentDevices;
        	totalLatency += curWindow.getLargestTakenSlot();
        	
        	// All devices transmitted packages in current trial
        	if (totalSentDevices >= maximumTestingDevicesForCurrentTrial) {
        		if (repeatition <= MAXIMUM_REPEATITION_FOR_EACH_SIMULATION) {
        			repeatition++;
        		} else {
        			double averageLatency = totalLatency / MAXIMUM_REPEATITION_FOR_EACH_SIMULATION;
//        			System.out.println("\nTotal latency = " + totalLatency);
//        			System.out.println("Average latency = " + averageLatency);
        			writer.println(averageLatency);
        			repeatition = 1;
        			totalLatency = 0;
        			maximumTestingDevicesForCurrentTrial += 100;
        			if (maximumTestingDevicesForCurrentTrial > numDevices) {
                        System.out.println("----- ENDING SIMULATION ----- \n");
                        System.out.println("--------------------------------- \n");
        				return;
        			}
        		}
        		
        		// Start new simulation
        		curWindow = new Window(initialWindowSize);
        		device = 1;
    			totalSentDevices = 0;
        	} else { // Need to use next window for transmitting
        		device = totalSentDevices + 1;
        		curWindow = bp.nextWindow(curWindow);
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

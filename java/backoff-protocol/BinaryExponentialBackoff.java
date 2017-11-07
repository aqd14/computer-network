/**
 * 
 */

/**
 * @author Anh Do
 * 
 * Implementation of binary exponential backoff protocol
 */
public class BinaryExponentialBackoff implements IBackoffProtocol {
    
    @Override
    public Window nextWindow(Window w) {
        assert w != null;
        int nextWindowSize = w.getNumSlots()*2;
        return new Window(nextWindowSize);
    }
    
    public static void main(String[] args) {
        Window w = new Window(2);
        System.out.println("Current window size = " + w.getNumSlots());
        BinaryExponentialBackoff beb = new BinaryExponentialBackoff();
        Window nextW = beb.nextWindow(w);
        assert nextW.getNumSlots() == 4;
        System.out.println("Next window size = " + nextW.getNumSlots());
    }
}

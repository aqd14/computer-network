/**
 * @author Anh Do
 * 
 * Implementation of trinary exponential backoff protocol
 */
public class TrinaryExponentialBackoff implements IBackoffProtocol {
    
    @Override
    public Window nextWindow(Window w) {
        assert w != null;
        int nextWindowSize = w.getNumSlots()*3;
        return new Window(nextWindowSize);
    }
    
    public static void main(String[] args) {
        Window w = new Window(3);
        System.out.println("Current window size = " + w.getNumSlots());
        TrinaryExponentialBackoff teb = new TrinaryExponentialBackoff();
        Window nextW = teb.nextWindow(w);
        assert w.getNumSlots() == 9;
        System.out.println("Next window size = " + nextW.getNumSlots());
    }
}

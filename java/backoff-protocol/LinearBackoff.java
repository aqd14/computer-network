/**
 * @author Anh Do
 *  
 * Implementation of linear backoff protocol
 */
public class LinearBackoff implements IBackoffProtocol {
    
    @Override
    public Window nextWindow(Window w) {
        assert w != null;
        int nextWindowSize = w.getNumSlots() + 1;
        w = null;
        return new Window(nextWindowSize);
    }
    
    public static void main(String[] args) {
        Window w = new Window(2);
        System.out.println("Current window size = " + w.getNumSlots());
        LinearBackoff lb = new LinearBackoff();
        Window nextW = lb.nextWindow(w);
        assert nextW.getNumSlots() == 3;
        System.out.println("Next window size = " + nextW.getNumSlots());
    }
}

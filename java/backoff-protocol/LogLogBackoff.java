/**
 * @author Anh Do
 * 
 * Implementation of log log backoff protocol
 */
public class LogLogBackoff implements IBackoffProtocol {
    
    @Override
    public Window nextWindow(Window w) {
        assert w != null;
        int nextWindowSize = (int) (1 + 1/logBaseTwo(logBaseTwo(w.getNumSlots()))) * 4;
        return new Window(nextWindowSize);
    }
    
    private double logBaseTwo(double a) {
        return Math.log(a)/Math.log(2);
    }
    
    public static void main(String[] args) {
        Window w = new Window(4);
        System.out.println("Current window size = " + w.getNumSlots());
        LogLogBackoff llb = new LogLogBackoff();
        Window nextW = llb.nextWindow(w);
        assert w.getNumSlots() == 8;
        System.out.println("Next window size = " + nextW.getNumSlots());
    }
}

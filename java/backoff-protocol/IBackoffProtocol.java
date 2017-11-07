/**
 * 
 * @author Anh Do
 *
 */
public interface IBackoffProtocol {
    /**
     * Get next window
     * @param w current window
     * @return next window
     */
    public Window nextWindow(Window w);
}

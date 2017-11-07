import java.util.Random;

/**
 * @author Anh Do
 * 
 * Simulation of window time frame. Each window contains a number of slots.
 */
public class Window {
    private int[] slots; // slots[i] = 0 => slot ith is available. Otherwise, taken
    Random slotGenerator = new Random();
    
    public Window(int numSlots) {
        slots = new int[numSlots];
    }
    
    public boolean isSlotTaken(int slotNum) {
        assert slots != null;
        assert isValidSlot(slotNum);
        return slots[slotNum] != 0;
    }

    public boolean isValidSlot(int slotNum) {
        return slotNum >= 0 && slotNum < slots.length;
    }
    public int getNumSlots() {
        return slots != null ? slots.length : 0;
    }

    public int getRandomSlotWithinWindow() {
        assert slots != null;
        return slotGenerator.nextInt(slots.length);
    }
    
    public void markSlotTaken(int slotNum) {
        if (isSlotTaken(slotNum) == false) {
            slots[slotNum] = 1;
        }
    }
}

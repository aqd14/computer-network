import java.util.Random;

/**
 * @author Anh Do
 * 
 * Simulation of window time frame. Each window contains a number of slots.
 */
public class Window {
    private int[] slots; // slots[i] = 0 => slot ith is available. Otherwise, taken
    private int largestTakenSlot;
    private Random slotGenerator;
    
    public Window(int numSlots) {
        slots = new int[numSlots];
        largestTakenSlot = -1;
        slotGenerator = new Random();
    }
    
    public boolean isSlotAvailable(int slotNum) {
        assert slots != null;
        assert isValidSlot(slotNum) == true;
        return slots[slotNum] == 0;
    }

    private boolean isValidSlot(int slotNum) {
        return slotNum >= 0 && slotNum < slots.length;
    }
    
    public int getNumSlots() {
        return slots != null ? slots.length : 0;
    }
    
    public int getLargestTakenSlot() {
    	return largestTakenSlot;
    }

    public void takeRandomSlotWithinWindow() {
        assert slots != null;
        int randSlot = 1 + slotGenerator.nextInt(slots.length); // Slot index starts at 1
        if (randSlot > largestTakenSlot) {
        	largestTakenSlot = randSlot;
        }
        slots[randSlot-1]++;
    }
    
    public int getEligibleSlots() {
    	int total = 0;
    	for (int i = 0; i < slots.length; i++) {
    		if (slots[i] == 1) {
    			total++;
    		}
    	}
    	return total;
    }
}

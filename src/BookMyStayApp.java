import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * CLASS - RoomInventory
 * ============================================================================
 * Use Case 3: Centralized Room Inventory Management
 * * Description:
 * This class acts as the single source of truth for room availability in the hotel.
 * Room pricing and characteristics are obtained from Room objects, not duplicated here.
 * This avoids multiple sources of truth and keeps responsibilities clearly separated.
 * * @version 3.0
 */
class RoomInventory {
    /**
     * Stores available room count for each room type.
     * Key -> Room type name
     * Value -> Available room count
     */
    private Map<String, Integer> roomAvailability;

    /**
     * Constructor initializes the inventory with default availability values.
     */
    public RoomInventory() {
        roomAvailability = new HashMap<>();
        initializeInventory();
    }

    /**
     * Initializes room availability data.
     * This method centralizes inventory setup instead of using scattered variables.
     */
    private void initializeInventory() {
        roomAvailability.put("Single Room", 5);
        roomAvailability.put("Double Room", 3);
        roomAvailability.put("Suite Room", 2);
    }

    /**
     * Returns the current availability map.
     * @return map of room type to available count
     */
    public Map<String, Integer> getRoomAvailability() {
        return roomAvailability;
    }

    /**
     * Updates availability for a specific room type.
     * @param roomType the room type to update
     * @param count new availability count
     */
    public void updateAvailability(String roomType, int count) {
        roomAvailability.put(roomType, count);
    }
}

// --- Domain Models ---

abstract class Room {
    protected String roomType;
    protected int beds;
    protected int size;
    protected double pricePerNight;

    public Room(String roomType, int beds, int size, double pricePerNight) {
        this.roomType = roomType;
        this.beds = beds;
        this.size = size;
        this.pricePerNight = pricePerNight;
    }

    public void displayRoomDetails(int availableRooms) {
        System.out.println(roomType + ":");
        System.out.println("Beds: " + beds);
        System.out.println("Size: " + size + " sqft");
        System.out.println("Price per night: " + pricePerNight);
        System.out.println("Available Rooms: " + availableRooms);
        System.out.println();
    }
}

class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 250, 1500.0);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 400, 2500.0);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 750, 5000.0);
    }
}

/**
 * ============================================================================
 * MAIN CLASS - BookMyStayApp
 * ============================================================================
 * Description:
 * This class demonstrates how room availability is managed using a centralized inventory.
 * Room objects are used to retrieve pricing and room characteristics.
 * No booking or search logic is introduced here.
 * * @version 3.0
 */
public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("Hotel Room Inventory Status\n");

        // 1. Initialize the Centralized Inventory (HashMap)
        RoomInventory inventory = new RoomInventory();
        Map<String, Integer> availability = inventory.getRoomAvailability();

        // 2. Initialize the Domain Objects
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        // 3. Display the combined data (Domain attributes + Inventory state)
        singleRoom.displayRoomDetails(availability.get("Single Room"));
        doubleRoom.displayRoomDetails(availability.get("Double Room"));
        suiteRoom.displayRoomDetails(availability.get("Suite Room"));
    }
}
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * CLASS - RoomInventory
 * ============================================================================
 * Use Case 3: Centralized Room Inventory Management
 */
class RoomInventory {
    private Map<String, Integer> roomAvailability;

    public RoomInventory() {
        roomAvailability = new HashMap<>();
        initializeInventory();
    }

    private void initializeInventory() {
        roomAvailability.put("Single Room", 5);
        roomAvailability.put("Double Room", 3);
        roomAvailability.put("Suite Room", 2);
    }

    public Map<String, Integer> getRoomAvailability() {
        return roomAvailability;
    }

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
        System.out.println("Available: " + availableRooms);
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
 * CLASS - RoomSearchService
 * ============================================================================
 * Use Case 4: Room Search & Availability Check
 *
 * Description:
 * This class provides search functionality for guests to view available rooms.
 * It reads room availability from inventory and room details from Room objects.
 * No inventory mutation or booking logic is performed in this class.
 *
 * @version 4.0
 */
class RoomSearchService {
    /**
     * Displays available rooms along with their details and pricing.
     * This method performs read-only access to inventory and room data.
     *
     * @param inventory   centralized room inventory
     * @param singleRoom  single room definition
     * @param doubleRoom  double room definition
     * @param suiteRoom   suite room definition
     */
    public void searchAvailableRooms(
            RoomInventory inventory,
            Room singleRoom,
            Room doubleRoom,
            Room suiteRoom) {

        Map<String, Integer> availability = inventory.getRoomAvailability();

        System.out.println("Room Search\n");

        // Check and display Single Room availability
        if (availability.getOrDefault("Single Room", 0) > 0) {
            singleRoom.displayRoomDetails(availability.get("Single Room"));
        }

        // Check and display Double Room availability
        if (availability.getOrDefault("Double Room", 0) > 0) {
            doubleRoom.displayRoomDetails(availability.get("Double Room"));
        }

        // Check and display Suite Room availability
        if (availability.getOrDefault("Suite Room", 0) > 0) {
            suiteRoom.displayRoomDetails(availability.get("Suite Room"));
        }
    }
}

/**
 * ============================================================================
 * MAIN CLASS - BookMyStayApp
 * ============================================================================
 * Description:
 * This class demonstrates how guests can view available rooms without
 * modifying inventory data. The system enforces read-only access by
 * design and usage discipline.
 *
 * @version 4.0
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        // 1. Initialize Inventory
        RoomInventory inventory = new RoomInventory();

        // 2. Initialize Domain Objects
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        // 3. Initialize Search Service
        RoomSearchService searchService = new RoomSearchService();

        // 4. Perform Read-Only Search
        searchService.searchAvailableRooms(inventory, singleRoom, doubleRoom, suiteRoom);
    }
}
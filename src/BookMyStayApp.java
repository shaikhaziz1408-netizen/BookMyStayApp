import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * ============================================================================
 * CLASS - RoomInventory
 * ============================================================================
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

class SingleRoom extends Room { public SingleRoom() { super("Single Room", 1, 250, 1500.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double Room", 2, 400, 2500.0); } }
class SuiteRoom extends Room { public SuiteRoom() { super("Suite Room", 3, 750, 5000.0); } }

/**
 * ============================================================================
 * CLASS - RoomSearchService
 * ============================================================================
 */
class RoomSearchService {
    public void searchAvailableRooms(RoomInventory inventory, Room singleRoom, Room doubleRoom, Room suiteRoom) {
        Map<String, Integer> availability = inventory.getRoomAvailability();
        if (availability.getOrDefault("Single Room", 0) > 0) singleRoom.displayRoomDetails(availability.get("Single Room"));
        if (availability.getOrDefault("Double Room", 0) > 0) doubleRoom.displayRoomDetails(availability.get("Double Room"));
        if (availability.getOrDefault("Suite Room", 0) > 0) suiteRoom.displayRoomDetails(availability.get("Suite Room"));
    }
}

/**
 * ============================================================================
 * CLASS - Reservation
 * ============================================================================
 */
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
}

/**
 * ============================================================================
 * CLASS - BookingRequestQueue
 * ============================================================================
 */
class BookingRequestQueue {
    private Queue<Reservation> requestQueue;

    public BookingRequestQueue() {
        this.requestQueue = new LinkedList<>();
    }

    public void enqueueRequest(Reservation request) {
        requestQueue.add(request);
        System.out.println("Queued: " + request.getGuestName() + " for a " + request.getRoomType());
    }

    // New methods for UC6 to allow processing
    public Reservation dequeueRequest() {
        return requestQueue.poll();
    }

    public boolean isEmpty() {
        return requestQueue.isEmpty();
    }
}

/**
 * ============================================================================
 * CLASS - RoomAllocationService
 * ============================================================================
 * Use Case 6: Reservation Confirmation & Room Allocation
 *
 * Description:
 * Processes queued booking requests and assigns rooms safely.
 * Uses a Set to ensure unique room IDs and prevent double-booking.
 * Updates inventory immediately after allocation.
 *
 * @version 6.0
 */
class RoomAllocationService {
    private RoomInventory inventory;

    // Maps a Room Type to a Set of uniquely allocated Room IDs
    private Map<String, Set<String>> allocatedRooms;

    // Simple counter to generate unique room IDs
    private int roomCounter = 100;

    public RoomAllocationService(RoomInventory inventory) {
        this.inventory = inventory;
        this.allocatedRooms = new HashMap<>();
    }

    public void processQueue(BookingRequestQueue queue) {
        System.out.println("\n--- Processing Booking Queue (FIFO) ---");

        while (!queue.isEmpty()) {
            Reservation request = queue.dequeueRequest();
            allocateRoom(request);
        }
        System.out.println("---------------------------------------\n");
    }

    private void allocateRoom(Reservation request) {
        String type = request.getRoomType();
        Map<String, Integer> currentInventory = inventory.getRoomAvailability();
        int availableCount = currentInventory.getOrDefault(type, 0);

        if (availableCount > 0) {
            // Generate a unique Room ID
            roomCounter++;
            String prefix = type.split(" ")[0].toUpperCase().substring(0, 3); // e.g., "SIN", "DOU", "SUI"
            String roomId = prefix + "-" + roomCounter;

            // Ensure uniqueness using a Set (Prevents Double-Booking)
            allocatedRooms.putIfAbsent(type, new HashSet<>());
            Set<String> typeAllocations = allocatedRooms.get(type);

            if (typeAllocations.contains(roomId)) {
                System.out.println("ERROR: Room ID " + roomId + " already in use! Double-booking prevented.");
                return;
            }

            // Record the allocation
            typeAllocations.add(roomId);

            // Update the inventory immediately
            inventory.updateAvailability(type, availableCount - 1);

            System.out.println("CONFIRMED: " + request.getGuestName() + " -> " + type + " (Assigned Room: " + roomId + ")");
        } else {
            System.out.println("FAILED: " + request.getGuestName() + " -> " + type + " is completely sold out.");
        }
    }
}

/**
 * ============================================================================
 * MAIN CLASS - BookMyStayApp
 * ============================================================================
 */
public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println("    === Book My Stay App (UC6) ===");
        System.out.println("=======================================\n");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        RoomAllocationService allocationService = new RoomAllocationService(inventory);

        // 1. Guests submit requests (Notice we request 3 Suites, but only 2 are in inventory)
        System.out.println("--- Incoming Requests ---");
        queue.enqueueRequest(new Reservation("Alice Smith", "Suite Room"));
        queue.enqueueRequest(new Reservation("Bob Johnson", "Double Room"));
        queue.enqueueRequest(new Reservation("Charlie Brown", "Suite Room"));
        queue.enqueueRequest(new Reservation("Diana Prince", "Suite Room")); // Should fail (only 2 suites exist)

        // 2. Process the queue
        allocationService.processQueue(queue);

        // 3. Verify Inventory was updated correctly
        System.out.println("--- Final Inventory Check ---");
        Map<String, Integer> finalInventory = inventory.getRoomAvailability();
        for (Map.Entry<String, Integer> entry : finalInventory.entrySet()) {
            System.out.println(entry.getKey() + " Remaining: " + entry.getValue());
        }
    }
}
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

    public Map<String, Integer> getRoomAvailability() { return roomAvailability; }
    public void updateAvailability(String roomType, int count) { roomAvailability.put(roomType, count); }
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
        System.out.println("Beds: " + beds + " | Size: " + size + " sqft | Price: $" + pricePerNight + " | Available: " + availableRooms);
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
        // Implementation hidden for brevity, same as UC4
    }
}

/**
 * ============================================================================
 * CLASS - Reservation & BookingRequestQueue
 * ============================================================================
 */
class Reservation {
    private String guestName;
    private String roomType;
    private String reservationId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
}

class BookingRequestQueue {
    private Queue<Reservation> requestQueue = new LinkedList<>();
    public void enqueueRequest(Reservation request) { requestQueue.add(request); }
    public Reservation dequeueRequest() { return requestQueue.poll(); }
    public boolean isEmpty() { return requestQueue.isEmpty(); }
}

/**
 * ============================================================================
 * CLASS - RoomAllocationService
 * ============================================================================
 */
class RoomAllocationService {
    private RoomInventory inventory;
    private Map<String, Set<String>> allocatedRooms;
    private int roomCounter = 100;

    public RoomAllocationService(RoomInventory inventory) {
        this.inventory = inventory;
        this.allocatedRooms = new HashMap<>();
    }

    public void processQueue(BookingRequestQueue queue) {
        while (!queue.isEmpty()) {
            allocateRoom(queue.dequeueRequest());
        }
    }

    private void allocateRoom(Reservation request) {
        String type = request.getRoomType();
        int availableCount = inventory.getRoomAvailability().getOrDefault(type, 0);

        if (availableCount > 0) {
            roomCounter++;
            String roomId = type.substring(0, 3).toUpperCase() + "-" + roomCounter;

            allocatedRooms.putIfAbsent(type, new HashSet<>());
            allocatedRooms.get(type).add(roomId);
            inventory.updateAvailability(type, availableCount - 1);

            // Assign the room ID as the reservation ID for tracking
            request.setReservationId(roomId);
            System.out.println("CONFIRMED: " + request.getGuestName() + " assigned Room " + roomId);
        } else {
            System.out.println("FAILED: " + type + " sold out for " + request.getGuestName());
        }
    }
}

/**
 * ============================================================================
 * CLASS - AddOnService (NEW FOR UC7)
 * ============================================================================
 * Represents an individual optional offering.
 */
class AddOnService {
    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() { return serviceName; }
    public double getCost() { return cost; }
}

/**
 * ============================================================================
 * CLASS - AddOnServiceManager (NEW FOR UC7)
 * ============================================================================
 * Manages the association between reservations and selected services.
 * Demonstrates a One-to-Many relationship using Map and List.
 */
class AddOnServiceManager {
    // Maps a Reservation ID to a List of AddOnServices
    private Map<String, List<AddOnService>> reservationServices;

    public AddOnServiceManager() {
        this.reservationServices = new HashMap<>();
    }

    public void addService(String reservationId, AddOnService service) {
        reservationServices.putIfAbsent(reservationId, new ArrayList<>());
        reservationServices.get(reservationId).add(service);
    }

    public void displayServicesAndCost(String reservationId) {
        List<AddOnService> services = reservationServices.getOrDefault(reservationId, new ArrayList<>());

        System.out.println("\n--- Add-On Services for Reservation: " + reservationId + " ---");

        if (services.isEmpty()) {
            System.out.println("No add-on services selected.");
            return;
        }

        double totalCost = 0.0;
        for (AddOnService service : services) {
            System.out.println(" + " + service.getServiceName() + ": $" + service.getCost());
            totalCost += service.getCost();
        }
        System.out.println("----------------------------------------------");
        System.out.println("Total Add-On Cost: $" + totalCost);
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
        System.out.println("    === Book My Stay App (UC7) ===");
        System.out.println("=======================================\n");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        RoomAllocationService allocationService = new RoomAllocationService(inventory);
        AddOnServiceManager addonManager = new AddOnServiceManager();

        // 1. Process a booking
        Reservation req1 = new Reservation("Alice Smith", "Suite Room");
        queue.enqueueRequest(req1);
        System.out.println("\nAllocating rooms...");
        allocationService.processQueue(queue);

        // 2. Define some Add-On Services
        AddOnService breakfast = new AddOnService("Complimentary Breakfast", 50.0);
        AddOnService spa = new AddOnService("Spa Access", 120.0);
        AddOnService airportPickup = new AddOnService("Airport Pickup", 75.0);

        // 3. Attach services to the confirmed reservation ID
        String aliceResId = req1.getReservationId();

        if(aliceResId != null) {
            addonManager.addService(aliceResId, breakfast);
            addonManager.addService(aliceResId, spa);

            // 4. Display the results
            addonManager.displayServicesAndCost(aliceResId);
        }
    }
}
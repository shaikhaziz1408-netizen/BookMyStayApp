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
 * CUSTOM EXCEPTION (NEW FOR UC9)
 * ============================================================================
 * Represents a domain-specific error when a booking violates business rules.
 */
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

/**
 * ============================================================================
 * CLASS - BookingValidator (NEW FOR UC9)
 * ============================================================================
 * Validates input and system state before processing requests.
 * Implements "Fail-Fast" design.
 */
class BookingValidator {
    public static void validateRequest(Reservation request, RoomInventory inventory) throws InvalidBookingException {
        // 1. Validate Guest Name
        if (request.getGuestName() == null || request.getGuestName().trim().isEmpty()) {
            throw new InvalidBookingException("Validation Failed: Guest name cannot be empty.");
        }

        // 2. Validate Room Type Existence
        if (!inventory.getRoomAvailability().containsKey(request.getRoomType())) {
            throw new InvalidBookingException("Validation Failed: Room type '" + request.getRoomType() + "' is invalid or unrecognized.");
        }
    }
}

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
        System.out.println(roomType + ": Beds: " + beds + " | Size: " + size + " sqft | Price: $" + pricePerNight + " | Available: " + availableRooms);
    }
}

class SingleRoom extends Room { public SingleRoom() { super("Single Room", 1, 250, 1500.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double Room", 2, 400, 2500.0); } }
class SuiteRoom extends Room { public SuiteRoom() { super("Suite Room", 3, 750, 5000.0); } }

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
 * CLASS - BookingHistory & ReportService
 * ============================================================================
 */
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();
    public void addRecord(Reservation reservation) { history.add(reservation); }
    public List<Reservation> getHistory() { return history; }
}

class BookingReportService {
    public void generateSummaryReport(BookingHistory history) {
        // Implementation hidden for brevity, same as UC8
    }
}

/**
 * ============================================================================
 * CLASS - RoomAllocationService
 * ============================================================================
 * Updated for UC9: Throws exceptions for sold-out rooms to prevent negative inventory.
 */
class RoomAllocationService {
    private RoomInventory inventory;
    private BookingHistory bookingHistory;
    private Map<String, Set<String>> allocatedRooms;
    private int roomCounter = 100;

    public RoomAllocationService(RoomInventory inventory, BookingHistory bookingHistory) {
        this.inventory = inventory;
        this.bookingHistory = bookingHistory;
        this.allocatedRooms = new HashMap<>();
    }

    public void processQueue(BookingRequestQueue queue) {
        while (!queue.isEmpty()) {
            Reservation request = queue.dequeueRequest();
            try {
                allocateRoom(request);
            } catch (InvalidBookingException e) {
                // Graceful failure handling
                System.out.println("ERROR PROCESSING REQUEST FOR " + request.getGuestName() + ": " + e.getMessage());
            }
        }
    }

    private void allocateRoom(Reservation request) throws InvalidBookingException {
        String type = request.getRoomType();
        int availableCount = inventory.getRoomAvailability().getOrDefault(type, 0);

        // UC9: Guarding System State
        if (availableCount <= 0) {
            throw new InvalidBookingException("Inventory Error: '" + type + "' is completely sold out. Cannot allocate room.");
        }

        roomCounter++;
        String roomId = type.substring(0, 3).toUpperCase() + "-" + roomCounter;

        allocatedRooms.putIfAbsent(type, new HashSet<>());
        allocatedRooms.get(type).add(roomId);
        inventory.updateAvailability(type, availableCount - 1);

        request.setReservationId(roomId);
        System.out.println("CONFIRMED: " + request.getGuestName() + " assigned Room " + roomId);

        bookingHistory.addRecord(request);
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
        System.out.println("    === Book My Stay App (UC9) ===");
        System.out.println("=======================================\n");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingHistory history = new BookingHistory();
        RoomAllocationService allocationService = new RoomAllocationService(inventory, history);

        System.out.println("--- Submitting Booking Requests ---");

        // Request 1: Valid
        submitRequest(new Reservation("Alice Smith", "Suite Room"), queue, inventory);

        // Request 2: Invalid Room Type
        submitRequest(new Reservation("Bob Johnson", "Penthouse"), queue, inventory);

        // Request 3: Invalid Name
        submitRequest(new Reservation("", "Double Room"), queue, inventory);

        // Request 4 & 5 & 6: Trigger Sold Out (Only 2 Suites exist)
        submitRequest(new Reservation("Charlie Brown", "Suite Room"), queue, inventory);
        submitRequest(new Reservation("Diana Prince", "Suite Room"), queue, inventory);

        System.out.println("\n--- Processing Queue ---");
        allocationService.processQueue(queue);
    }

    /**
     * Helper method to validate before enqueueing.
     */
    private static void submitRequest(Reservation request, BookingRequestQueue queue, RoomInventory inventory) {
        try {
            // Fail-Fast: Validate before it even enters the queue
            BookingValidator.validateRequest(request, inventory);
            queue.enqueueRequest(request);
            System.out.println("SUCCESS: Request accepted for " + request.getGuestName());
        } catch (InvalidBookingException e) {
            System.out.println("REJECTED: " + e.getMessage());
        }
    }
}
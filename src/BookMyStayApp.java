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
 * CLASS - BookingHistory (NEW FOR UC8)
 * ============================================================================
 * Maintains a record of confirmed reservations using a List to preserve
 * insertion order, acting as our persistence layer.
 */
class BookingHistory {
    private List<Reservation> history;

    public BookingHistory() {
        this.history = new ArrayList<>();
    }

    public void addRecord(Reservation reservation) {
        history.add(reservation);
    }

    public List<Reservation> getHistory() {
        return history;
    }
}

/**
 * ============================================================================
 * CLASS - RoomAllocationService
 * ============================================================================
 * Updated for UC8: Now injects BookingHistory and saves confirmed records.
 */
class RoomAllocationService {
    private RoomInventory inventory;
    private BookingHistory bookingHistory;
    private Map<String, Set<String>> allocatedRooms;
    private int roomCounter = 100;

    // Added BookingHistory to constructor
    public RoomAllocationService(RoomInventory inventory, BookingHistory bookingHistory) {
        this.inventory = inventory;
        this.bookingHistory = bookingHistory;
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

            request.setReservationId(roomId);
            System.out.println("CONFIRMED: " + request.getGuestName() + " assigned Room " + roomId);

            // UC8: Save to historical audit trail immediately after confirmation
            bookingHistory.addRecord(request);
        } else {
            System.out.println("FAILED: " + type + " sold out for " + request.getGuestName());
        }
    }
}

/**
 * ============================================================================
 * CLASS - BookingReportService (NEW FOR UC8)
 * ============================================================================
 * Generates summaries and reports from stored booking data without modifying it.
 */
class BookingReportService {
    public void generateSummaryReport(BookingHistory history) {
        List<Reservation> records = history.getHistory();

        System.out.println("\n=======================================");
        System.out.println("      ADMIN: BOOKING HISTORY REPORT    ");
        System.out.println("=======================================");

        if (records.isEmpty()) {
            System.out.println("No confirmed bookings found.");
        } else {
            System.out.println("Total Confirmed Bookings: " + records.size() + "\n");
            for (int i = 0; i < records.size(); i++) {
                Reservation res = records.get(i);
                System.out.println((i + 1) + ". [ID: " + res.getReservationId() + "] Guest: " +
                        res.getGuestName() + " | Room: " + res.getRoomType());
            }
        }
        System.out.println("=======================================\n");
    }
}

// ... AddOnService and AddOnServiceManager remain identical to UC7 ...
class AddOnService {
    private String serviceName;
    private double cost;
    public AddOnService(String serviceName, double cost) { this.serviceName = serviceName; this.cost = cost; }
    public String getServiceName() { return serviceName; }
    public double getCost() { return cost; }
}

class AddOnServiceManager {
    private Map<String, List<AddOnService>> reservationServices = new HashMap<>();
    public void addService(String reservationId, AddOnService service) {
        reservationServices.putIfAbsent(reservationId, new ArrayList<>());
        reservationServices.get(reservationId).add(service);
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
        System.out.println("    === Book My Stay App (UC8) ===");
        System.out.println("=======================================\n");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingHistory history = new BookingHistory(); // UC8 initialized

        // Pass the history into the allocation service so it can record confirmations
        RoomAllocationService allocationService = new RoomAllocationService(inventory, history);
        BookingReportService reportService = new BookingReportService(); // UC8 initialized

        // 1. Process multiple bookings
        queue.enqueueRequest(new Reservation("Alice Smith", "Suite Room"));
        queue.enqueueRequest(new Reservation("Bob Johnson", "Double Room"));
        queue.enqueueRequest(new Reservation("Charlie Brown", "Single Room"));

        System.out.println("\nProcessing queue and allocating rooms...");
        allocationService.processQueue(queue);

        // 2. Admin requests the Booking History Report
        reportService.generateSummaryReport(history);
    }
}
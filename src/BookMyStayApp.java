import java.util.*;

/**
 * ============================================================================
 * CUSTOM EXCEPTION
 * ============================================================================
 */
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) { super(message); }
}

/**
 * ============================================================================
 * CLASS - RoomInventory
 * ============================================================================
 */
class RoomInventory {
    private Map<String, Integer> roomAvailability = new HashMap<>();

    public RoomInventory() {
        roomAvailability.put("Single Room", 5);
        roomAvailability.put("Double Room", 3);
        roomAvailability.put("Suite Room", 2);
    }

    public Map<String, Integer> getRoomAvailability() { return roomAvailability; }
    public void updateAvailability(String roomType, int count) { roomAvailability.put(roomType, count); }
}

// --- Domain Models ---
class Reservation {
    private String guestName;
    private String roomType;
    private String reservationId;
    private boolean isCancelled = false;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { isCancelled = cancelled; }
}

/**
 * ============================================================================
 * CLASS - BookingHistory
 * ============================================================================
 */
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();
    public void addRecord(Reservation reservation) { history.add(reservation); }
    public List<Reservation> getHistory() { return history; }

    // Helper to find a reservation for cancellation
    public Reservation findById(String id) {
        for (Reservation r : history) {
            if (id.equals(r.getReservationId())) return r;
        }
        return null;
    }
}

/**
 * ============================================================================
 * CLASS - CancellationService (NEW FOR UC10)
 * ============================================================================
 * Handles state reversal using a Stack for LIFO rollback logic.
 * Ensures inventory consistency when a booking is undone.
 */
class CancellationService {
    private RoomInventory inventory;
    private BookingHistory history;

    // Stack tracks released Room IDs for potential audit or reuse
    private Stack<String> releasedRoomStack;

    public CancellationService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
        this.releasedRoomStack = new Stack<>();
    }

    /**
     * Reverses a confirmed booking and restores inventory.
     * * @param reservationId the ID to cancel
     * @throws InvalidBookingException if ID is invalid or already cancelled
     */
    public void cancelBooking(String reservationId) throws InvalidBookingException {
        Reservation res = history.findById(reservationId);

        // 1. Validation
        if (res == null) {
            throw new InvalidBookingException("Cancellation Failed: Reservation ID '" + reservationId + "' not found.");
        }
        if (res.isCancelled()) {
            throw new InvalidBookingException("Cancellation Failed: Reservation '" + reservationId + "' is already cancelled.");
        }

        // 2. Perform Rollback Logic
        String type = res.getRoomType();
        int currentCount = inventory.getRoomAvailability().get(type);

        // 3. Update State
        inventory.updateAvailability(type, currentCount + 1);
        res.setCancelled(true);

        // 4. LIFO Rollback Tracking
        releasedRoomStack.push(res.getReservationId());

        System.out.println("CANCELLED: " + res.getGuestName() + "'s booking [" + reservationId + "] has been reversed.");
        System.out.println("LOG: Room " + reservationId + " pushed to LIFO rollback stack. New Inventory: " + (currentCount + 1));
    }
}

/**
 * ============================================================================
 * CLASS - RoomAllocationService
 * ============================================================================
 */
class RoomAllocationService {
    private RoomInventory inventory;
    private BookingHistory bookingHistory;
    private int roomCounter = 100;

    public RoomAllocationService(RoomInventory inventory, BookingHistory bookingHistory) {
        this.inventory = inventory;
        this.bookingHistory = bookingHistory;
    }

    public void allocateRoom(Reservation request) throws InvalidBookingException {
        String type = request.getRoomType();
        int availableCount = inventory.getRoomAvailability().getOrDefault(type, 0);

        if (availableCount <= 0) {
            throw new InvalidBookingException("Inventory Error: '" + type + "' sold out.");
        }

        roomCounter++;
        String roomId = type.substring(0, 3).toUpperCase() + "-" + roomCounter;

        inventory.updateAvailability(type, availableCount - 1);
        request.setReservationId(roomId);
        bookingHistory.addRecord(request);

        System.out.println("CONFIRMED: " + request.getGuestName() + " assigned Room " + roomId);
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
        System.out.println("    === Book My Stay App (UC10) ===");
        System.out.println("=======================================\n");

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        RoomAllocationService allocationService = new RoomAllocationService(inventory, history);
        CancellationService cancellationService = new CancellationService(inventory, history);

        try {
            // 1. Create and Process Bookings
            Reservation res1 = new Reservation("Alice Smith", "Suite Room");
            Reservation res2 = new Reservation("Bob Johnson", "Single Room");

            allocationService.allocateRoom(res1);
            allocationService.allocateRoom(res2);

            System.out.println("\n--- Current Inventory Status ---");
            System.out.println("Suite Rooms: " + inventory.getRoomAvailability().get("Suite Room"));

            // 2. Perform Cancellation (Rollback)
            System.out.println("\n--- Initiating Cancellation ---");
            cancellationService.cancelBooking(res1.getReservationId());

            // 3. Verify Inventory Restoration
            System.out.println("\n--- Final Inventory Status ---");
            System.out.println("Suite Rooms (After Rollback): " + inventory.getRoomAvailability().get("Suite Room"));

            // 4. Test Invalid Cancellation
            System.out.println("\n--- Testing Invalid Cancellation ---");
            cancellationService.cancelBooking("NON-EXISTENT-ID");

        } catch (InvalidBookingException e) {
            System.out.println("SYSTEM_NOTICE: " + e.getMessage());
        }
    }
}
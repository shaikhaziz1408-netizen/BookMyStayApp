import java.util.*;

/**
 * ============================================================================
 * CLASS - RoomInventory (Thread-Safe)
 * ============================================================================
 * Updated for UC11: Uses synchronized methods to prevent race conditions
 * during concurrent inventory updates.
 */
class RoomInventory {
    private final Map<String, Integer> roomAvailability = new HashMap<>();

    public RoomInventory() {
        roomAvailability.put("Single Room", 5);
        roomAvailability.put("Double Room", 3);
        roomAvailability.put("Suite Room", 2);
    }

    // Synchronized to ensure only one thread can check/update at a time
    public synchronized Map<String, Integer> getRoomAvailability() {
        return new HashMap<>(roomAvailability); // Return a copy for thread safety
    }

    public synchronized void updateAvailability(String roomType, int count) {
        roomAvailability.put(roomType, count);
    }
}

// --- Domain Models ---
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

/**
 * ============================================================================
 * CLASS - BookingHistory (Thread-Safe)
 * ============================================================================
 */
class BookingHistory {
    // Vector is a legacy thread-safe List, or use Collections.synchronizedList
    private final List<Reservation> history = Collections.synchronizedList(new ArrayList<>());

    public void addRecord(Reservation reservation) {
        history.add(reservation);
    }

    public List<Reservation> getHistory() {
        return new ArrayList<>(history);
    }
}

/**
 * ============================================================================
 * CLASS - RoomAllocationService (Thread-Safe)
 * ============================================================================
 * Use Case 11: Concurrent Booking Simulation
 * Handles allocation logic inside synchronized blocks to prevent double-booking.
 */
class RoomAllocationService {
    private final RoomInventory inventory;
    private final BookingHistory bookingHistory;
    private int roomCounter = 100;

    public RoomAllocationService(RoomInventory inventory, BookingHistory bookingHistory) {
        this.inventory = inventory;
        this.bookingHistory = bookingHistory;
    }

    /**
     * The critical section is synchronized on the service instance to ensure
     * that room IDs and inventory are updated atomically.
     */
    public synchronized void allocateRoom(Reservation request) {
        String type = request.getRoomType();
        int availableCount = inventory.getRoomAvailability().getOrDefault(type, 0);

        if (availableCount > 0) {
            // Simulated delay to expose potential race conditions if not synchronized
            try { Thread.sleep(10); } catch (InterruptedException e) {}

            roomCounter++;
            String roomId = type.substring(0, 3).toUpperCase() + "-" + roomCounter;

            inventory.updateAvailability(type, availableCount - 1);
            request.setReservationId(roomId);
            bookingHistory.addRecord(request);

            System.out.println("[THREAD: " + Thread.currentThread().getName() + "] CONFIRMED: "
                    + request.getGuestName() + " -> " + roomId);
        } else {
            System.out.println("[THREAD: " + Thread.currentThread().getName() + "] FAILED: "
                    + type + " sold out for " + request.getGuestName());
        }
    }
}

/**
 * ============================================================================
 * MAIN CLASS - BookMyStayApp
 * ============================================================================
 * Simulates multiple threads (guests) attempting to book at the same time.
 */
public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println("    === Book My Stay App (UC11) ===");
        System.out.println("    Concurrent Booking Simulation");
        System.out.println("=======================================\n");

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        RoomAllocationService allocationService = new RoomAllocationService(inventory, history);

        // Define a list of guest requests
        List<Reservation> requests = Arrays.asList(
                new Reservation("Guest_1", "Suite Room"),
                new Reservation("Guest_2", "Suite Room"),
                new Reservation("Guest_3", "Suite Room"), // Only 2 suites exist!
                new Reservation("Guest_4", "Single Room"),
                new Reservation("Guest_5", "Single Room")
        );

        // Create and start a thread for each request
        List<Thread> threads = new ArrayList<>();
        for (Reservation res : requests) {
            Thread t = new Thread(() -> allocationService.allocateRoom(res), res.getGuestName());
            threads.add(t);
            t.start();
        }

        // Wait for all threads to finish
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException e) {}
        }

        System.out.println("\n--- Final System State Verification ---");
        System.out.println("Total Bookings in History: " + history.getHistory().size());
        System.out.println("Remaining Suite Rooms: " + inventory.getRoomAvailability().get("Suite Room"));
    }
}
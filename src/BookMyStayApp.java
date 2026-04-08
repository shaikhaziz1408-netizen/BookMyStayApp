import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
 */
class RoomSearchService {
    public void searchAvailableRooms(RoomInventory inventory, Room singleRoom, Room doubleRoom, Room suiteRoom) {
        Map<String, Integer> availability = inventory.getRoomAvailability();
        System.out.println("Room Search\n");

        if (availability.getOrDefault("Single Room", 0) > 0) {
            singleRoom.displayRoomDetails(availability.get("Single Room"));
        }
        if (availability.getOrDefault("Double Room", 0) > 0) {
            doubleRoom.displayRoomDetails(availability.get("Double Room"));
        }
        if (availability.getOrDefault("Suite Room", 0) > 0) {
            suiteRoom.displayRoomDetails(availability.get("Suite Room"));
        }
    }
}

/**
 * ============================================================================
 * CLASS - Reservation
 * ============================================================================
 * Use Case 5: Booking Request (FIFO)
 *
 * Description:
 * This class represents a booking request made by a guest.
 * At this stage, a reservation only captures intent, not confirmation or room allocation.
 *
 * @version 5.0
 */
class Reservation {

    /** Name of the guest making the booking. */
    private String guestName;

    /** Requested room type. */
    private String roomType;

    /**
     * Creates a new booking request.
     *
     * @param guestName name of the guest
     * @param roomType requested room type
     */
    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    /** @return guest name */
    public String getGuestName() { return guestName; }

    /** @return requested room type */
    public String getRoomType() { return roomType; }
}

/**
 * ============================================================================
 * CLASS - BookingRequestQueue
 * ============================================================================
 * Use Case 5: Booking Request (FIFO)
 *
 * Description:
 * This class manages booking requests using a queue to ensure fair allocation.
 * Requests are processed strictly in the order they are received.
 *
 * @version 5.0
 */
class BookingRequestQueue {

    /** Queue to store reservations in FIFO order */
    private Queue<Reservation> requestQueue;

    public BookingRequestQueue() {
        // LinkedList is a standard implementation of the Queue interface in Java
        this.requestQueue = new LinkedList<>();
    }

    /**
     * Adds a new reservation request to the back of the queue.
     */
    public void enqueueRequest(Reservation request) {
        requestQueue.add(request);
        System.out.println("Request added to queue: " + request.getGuestName() + " -> " + request.getRoomType());
    }

    /**
     * Displays all current requests waiting in the queue.
     */
    public void displayQueue() {
        System.out.println("\n--- Current Booking Queue (FIFO Order) ---");
        if (requestQueue.isEmpty()) {
            System.out.println("No pending requests.");
        } else {
            int position = 1;
            for (Reservation req : requestQueue) {
                System.out.println(position + ". Guest: " + req.getGuestName() + " | Requested: " + req.getRoomType());
                position++;
            }
        }
        System.out.println("------------------------------------------\n");
    }
}

/**
 * ============================================================================
 * MAIN CLASS - BookMyStayApp
 * ============================================================================
 * Description:
 * This class demonstrates the intake of booking requests using a FIFO queue.
 * No inventory mutation occurs at this stage; we are simply storing intent fairly.
 *
 * @version 5.0
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=======================================");
        System.out.println("    === Book My Stay App (UC5) ===");
        System.out.println("=======================================\n");

        // 1. Initialize the Request Queue
        BookingRequestQueue queue = new BookingRequestQueue();

        // 2. Create some sample booking requests representing peak demand
        Reservation req1 = new Reservation("Alice Smith", "Single Room");
        Reservation req2 = new Reservation("Bob Johnson", "Double Room");
        Reservation req3 = new Reservation("Charlie Brown", "Suite Room");
        Reservation req4 = new Reservation("Diana Prince", "Single Room");

        // 3. Guests submit requests (Added to queue in arrival order)
        queue.enqueueRequest(req1);
        queue.enqueueRequest(req2);
        queue.enqueueRequest(req3);
        queue.enqueueRequest(req4);

        // 4. Display the queue to prove FIFO order is maintained
        queue.displayQueue();

        System.out.println("Requests queued successfully. Awaiting allocation process...");
    }
}
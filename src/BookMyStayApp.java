/**
 * ============================================================================
 * MAIN CLASS - BookMyStayApp
 * ============================================================================
 *
 * Use Case 2: Basic Room Types & Static Availability
 *
 * Description:
 * This class introduces object modeling through inheritance and abstraction.
 * We define an abstract Room class and concrete implementations for different
 * room types. Availability is stored statically using simple variables.
 *
 * @author Developer
 * @version 2.0
 */

// 1. Abstract Class: Defines the generalized concept of a Room
abstract class Room {
    protected String roomType;
    protected double pricePerNight;
    protected int capacity;

    public Room(String roomType, double pricePerNight, int capacity) {
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
    }

    // Encapsulated behavior to display room characteristics
    public void displayRoomDetails() {
        System.out.println("Room Type: " + roomType + " | Capacity: " + capacity + " | Price/Night: $" + pricePerNight);
    }
}

// 2. Inheritance: Concrete classes specializing the abstract Room class
class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 100.0, 1);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 150.0, 2);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite", 250.0, 4);
    }
}

// Main Application Class
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=======================================");
        System.out.println("       === Book My Stay App ===");
        System.out.println("=======================================\n");

        // 3. Polymorphism: Initialize Room Objects using the Room reference type
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        // 4. Static Availability Representation (Simple variables instead of data structures)
        int singleRoomAvailability = 10;
        int doubleRoomAvailability = 5;
        int suiteRoomAvailability = 2;

        System.out.println("--- Current Room Availability ---");

        singleRoom.displayRoomDetails();
        System.out.println("Available Rooms: " + singleRoomAvailability + "\n");

        doubleRoom.displayRoomDetails();
        System.out.println("Available Rooms: " + doubleRoomAvailability + "\n");

        suiteRoom.displayRoomDetails();
        System.out.println("Available Rooms: " + suiteRoomAvailability + "\n");

        System.out.println("System ready for operations...");
    }
}
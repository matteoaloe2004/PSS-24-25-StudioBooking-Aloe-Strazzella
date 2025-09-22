import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.dao.BookingDAO;
import com.example.studiobooking.model.Booking;

public class BookingDAOTest {

    private BookingDAO bookingDAO;

    @BeforeEach
    void setup() {
        bookingDAO = new BookingDAO();
        System.out.println("Setup: BookingDAO initialized");
    }

    @Test
    void testCreateAndRetrieveBooking() {
        // Creiamo un booking con dati fittizi
        Booking booking = new Booking(0, 34, "Matteo", 1,
                LocalDateTime.of(2026, 9, 22, 10, 0),
                LocalDateTime.of(2026, 9, 22, 12, 0),
                "CONFIRMED");

        System.out.println("Attempting to create booking for user: " + booking.getUserName());

        boolean created = bookingDAO.createBooking(
                booking.getUserId(),
                booking.getStudioId(),
                booking.getStartTime(),
                booking.getEndTime(),
                new ArrayList<>() // lista equipment vuota
        );

        System.out.println("Booking created? " + created);

        if (!created) {
            System.out.println("Booking creation failed! Possible reasons:");
            System.out.println("- Time conflict detected");
            System.out.println("- Database error or missing table/column");
            System.out.println("- Incorrect connection or permissions");
        }

        // Recuperiamo il booking dal DB
        // ATTENZIONE: bisogna usare l'ID generato dal DB. In createBooking attualmente non viene restituito.
        // Qui possiamo recuperare l'ultimo inserito per il test.
        Booking retrieved = null;
        try {
            // Tentativo di recuperare l'ultima prenotazione per l'utente
            retrieved = bookingDAO.getBookingsByUser(booking.getUserId())
                    .stream()
                    .filter(b -> b.getStudioId() == booking.getStudioId()
                            && b.getStartTime().equals(booking.getStartTime()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.out.println("Error retrieving booking: " + e.getMessage());
        }

        System.out.println("Retrieved booking: " + retrieved);

        assertNotNull(retrieved, "Booking should not be null");
        assertEquals(booking.getUserName(), retrieved.getUserName(), "UserName should match");
    }
}

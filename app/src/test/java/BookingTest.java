import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;   // percorso corretto della classe Booking

import com.example.studiobooking.model.Booking;

public class BookingTest {

    @Test
    void testBookingToString() {
        LocalDateTime start = LocalDateTime.of(2025, 9, 22, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 9, 22, 12, 0);
        Booking booking = new Booking(1, 34, "Matteo", 2, start, end, "CONFIRMED");

        String expected = "Prenotazione #1 | Utente: Matteo | Studio: 2 | 22/09/2025 10:00 → 22/09/2025 12:00 | Stato: CONFIRMED";
        assertEquals(expected, booking.toString());
    }

    @Test
    void testSetStatus() {
        Booking booking = new Booking(1, 34, "Matteo", 2, LocalDateTime.now(), LocalDateTime.now().plusHours(2), "CONFIRMED");
        booking.setStatus("CANCELLED");
        assertEquals("CANCELLED", booking.getStatus());
    }
}

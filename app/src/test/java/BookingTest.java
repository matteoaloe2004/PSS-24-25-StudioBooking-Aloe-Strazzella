import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.model.Booking;

public class BookingTest {

    @Test
    void testBookingToString() {
        System.out.println("=== Inizio testBookingToString ===");
        LocalDateTime start = LocalDateTime.of(2025, 9, 22, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 9, 22, 12, 0);
        Booking booking = new Booking(1, 34, "Matteo", 2, start, end, "CONFIRMED");

        System.out.println("Booking creato: " + booking);

        String expected = "Prenotazione #1 | Utente: Matteo | Studio: 2 | 22/09/2025 10:00 → 22/09/2025 12:00 | Stato: CONFIRMED";
        assertEquals(expected, booking.toString());

        System.out.println("TestBookingToString completato con successo");
    }

    @Test
    void testSetStatus() {
        System.out.println("=== Inizio testSetStatus ===");
        Booking booking = new Booking(1, 34, "Matteo", 2, LocalDateTime.now(), LocalDateTime.now().plusHours(2), "CONFIRMED");

        System.out.println("Stato iniziale: " + booking.getStatus());
        booking.setStatus("CANCELLED");
        System.out.println("Stato aggiornato: " + booking.getStatus());

        assertEquals("CANCELLED", booking.getStatus());
        System.out.println("TestSetStatus completato con successo");
    }
}

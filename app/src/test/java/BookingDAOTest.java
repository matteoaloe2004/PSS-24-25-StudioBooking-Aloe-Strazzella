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
        System.out.println("Inizio test BookingDAO");
    }

    @Test
    void testCreateAndRetrieveBooking() {
        // Creiamo un booking con dati fittizi
        Booking booking = new Booking(0, 34, "Matteo", 1,
                LocalDateTime.of(2026, 10, 22, 10, 0),
                LocalDateTime.of(2026, 10, 22, 12, 0),
                "CONFIRMED");

        System.out.println("Tentativo di creare una prenotazione per l'utente: " + booking.getUserName());

        boolean created = bookingDAO.createBooking(
                booking.getUserId(),
                booking.getStudioId(),
                booking.getStartTime(),
                booking.getEndTime(),
                new ArrayList<>() // lista equipment vuota
        );

        if (created) {
            System.out.println("Prenotazione creata con successo!");
        } else {
            System.out.println("Creazione della prenotazione fallita! Possibili motivi:");
            System.out.println("- Conflitto di orario rilevato");
            System.out.println("- Errore del database o tabella/colonna mancante");
            System.out.println("- Connessione errata o permessi insufficienti");
        }

        assertEquals(true, created, "La prenotazione dovrebbe essere creata con successo");

        // Recuperiamo il booking dal DB in modo più robusto
        Booking retrieved = null;
        try {
            retrieved = bookingDAO.getBookingsByUser(booking.getUserId())
                    .stream()
                    .filter(b -> b.getStudioId() == booking.getStudioId()
                            && b.getStartTime().toLocalDate().equals(booking.getStartTime().toLocalDate())
                            && b.getStartTime().getHour() == booking.getStartTime().getHour()
                            && b.getStartTime().getMinute() == booking.getStartTime().getMinute())
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.out.println("Errore durante il recupero della prenotazione: " + e.getMessage());
        }

        if (retrieved != null) {
            System.out.println("Prenotazione recuperata dal DB:");
            System.out.println("ID: " + retrieved.getId());
            System.out.println("Utente: " + retrieved.getUserName());
            System.out.println("Studio: " + retrieved.getStudioId());
            System.out.println("Orario: " + retrieved.getStartTime() + " → " + retrieved.getEndTime());
            System.out.println("Stato: " + retrieved.getStatus());
        } else {
            System.out.println("Nessuna prenotazione trovata nel DB corrispondente ai criteri.");
        }

        assertNotNull(retrieved, "La prenotazione non dovrebbe essere null");
        assertEquals(booking.getUserName(), retrieved.getUserName(), "Il nome utente dovrebbe corrispondere");

        // --- Cleanup: elimina la prenotazione appena creata ---
        if (retrieved != null) {
            boolean deleted = bookingDAO.deleteBooking(retrieved.getId());
            System.out.println("Prenotazione eliminata: " + deleted);
        }
    }
}

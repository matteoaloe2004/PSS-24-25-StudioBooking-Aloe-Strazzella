import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;   // percorso corretto della classe Booking
import org.junit.jupiter.api.Test;  // percorso corretto della classe BookingDAO

import com.example.studiobooking.dao.BookingDAO;
import com.example.studiobooking.model.Booking;



public class BookingDAOTest {

    private BookingDAO bookingDAO;

    @BeforeEach
    void setup() {
        bookingDAO = new BookingDAO();
    }

    @Test
    void testCreateAndRetrieveBooking() {
        Booking booking = new Booking(0, 100, "Matteo", 1, 
                LocalDateTime.of(2025,9,22,10,0), 
                LocalDateTime.of(2025,9,22,12,0), 
                "CONFIRMED");

        bookingDAO.save(booking);
        Booking retrieved = bookingDAO.findById(booking.getId());

        assertNotNull(retrieved);
        assertEquals(booking.getUserName(), retrieved.getUserName());
    }
}

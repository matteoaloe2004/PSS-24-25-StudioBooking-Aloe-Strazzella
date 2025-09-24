import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.model.Utente;

public class UtenteTest {

    @Test
    void testGettersAndSetters() {
        System.out.println("=== Inizio test Utente ===");

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Utente u = new Utente(1, "Mario Rossi", "mario@example.com", "password", now, false);

        System.out.println("Utente creato: id=" + u.getId() + ", name=" + u.getName() +
                           ", email=" + u.getEmail() + ", admin=" + u.isAdmin());

        assertEquals(1, u.getId());
        assertEquals("Mario Rossi", u.getName());
        assertEquals("mario@example.com", u.getEmail());
        assertEquals("password", u.getPassword());
        assertEquals(now, u.getCreatedAt());
        assertFalse(u.isAdmin());

        System.out.println("Aggiornamento valori name, email, password e admin");
        u.setName("Luigi Bianchi");
        u.setEmail("luigi@example.com");
        u.setPassword("1234");
        u.setAdmin(true);

        System.out.println("Valori aggiornati: name=" + u.getName() + ", email=" + u.getEmail() +
                           ", password=" + u.getPassword() + ", admin=" + u.isAdmin());

        assertEquals("Luigi Bianchi", u.getName());
        assertEquals("luigi@example.com", u.getEmail());
        assertEquals("1234", u.getPassword());
        assertTrue(u.isAdmin());

        System.out.println("Test Utente completato con successo");
    }
}

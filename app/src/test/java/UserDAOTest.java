import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.model.Utente;

public class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    void setup() {
        userDAO = new UserDAO();
        System.out.println("=== Inizio test UserDAO ===");
    }

    @Test
    void testRegisterAndRetrieveUser() {
        Utente user = new Utente(0, "Test User", "testuser@example.com", "password123", new Timestamp(System.currentTimeMillis()), false);

        // Registrazione
        boolean created = userDAO.register(user);
        System.out.println("Utente registrato? " + created);
        assertTrue(created);

        // Recupero
        Utente retrieved = userDAO.getUserByEmail("testuser@example.com");
        System.out.println("Utente recuperato: " + retrieved);
        assertNotNull(retrieved);
        assertEquals("Test User", retrieved.getName());

        // --- Cleanup: elimina l'utente appena creato ---
        if (retrieved != null) {
            boolean deleted = userDAO.deleteUser(retrieved.getId());
            System.out.println("Utente eliminato: " + deleted);
        }
    }
}

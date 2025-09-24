import java.sql.Timestamp;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.dao.LoyaltyCardDAO;
import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.model.LoyaltyCard;
import com.example.studiobooking.model.Utente;

public class LoyaltyCardDAOTest {

    private LoyaltyCardDAO cardDAO;
    private UserDAO userDAO;
    private Utente testUser;

    @BeforeEach
    void setup() {
        cardDAO = new LoyaltyCardDAO();
        userDAO = new UserDAO();

        // Creazione utente unico per test
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        testUser = new Utente(0, "Test User", email, "pass123", new Timestamp(System.currentTimeMillis()), false);
        boolean createdUser = userDAO.register(testUser);
        System.out.println("=== Inizio test LoyaltyCardDAO ===");
        System.out.println("Utente di test creato: " + createdUser);

        // Recuperiamo l'ID reale
        testUser = userDAO.getUserByEmail(email);
        System.out.println("ID utente test: " + (testUser != null ? testUser.getId() : "null"));
    }

    @AfterEach
    void cleanup() {
        if (testUser != null) {
            try {
                System.out.println("Pulizia: rimuovo loyalty card e utente di test");
                LoyaltyCard card = cardDAO.getLoyaltyCardByUserId(testUser.getId());
                if (card != null) {
                    // opzionale: log aggiornamento
                    cardDAO.refreshLoyaltyCard(testUser.getId());
                }
                userDAO.deleteUser(testUser.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testCreateRetrieveUpdateCard() {
        // Creazione loyalty card
        boolean created = cardDAO.createLoyaltyCard(testUser.getId());
        System.out.println("Loyalty card creata: " + created);
        assertEquals(true, created);

        // Recupero
        LoyaltyCard card = cardDAO.getLoyaltyCardByUserId(testUser.getId());
        System.out.println("Card recuperata: id=" + card.getId() + ", userId=" + card.getUserId()
                + ", totalBooking=" + card.getTotalBooking() + ", discountLevel=" + card.getDiscountLevel());
        assertNotNull(card);

        // Aggiorno valori
        System.out.println("Aggiornamento valori totalBooking e discountLevel");
        boolean updated = cardDAO.updateDiscountLevel(testUser.getId(), 7);
        assertEquals(true, updated);

        LoyaltyCard updatedCard = cardDAO.getLoyaltyCardByUserId(testUser.getId());
        System.out.println("Valori aggiornati: totalBooking=" + updatedCard.getTotalBooking()
                + ", discountLevel=" + updatedCard.getDiscountLevel());

        assertEquals(7, updatedCard.getTotalBooking());
        assertEquals(10, updatedCard.getDiscountLevel()); // 7/3*5 = 10
        System.out.println("Test LoyaltyCard completato con successo");
    }
}

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.model.LoyaltyCard;

public class LoyaltyCardTest {

    @Test
    void testPropertiesAndGettersSetters() {
        System.out.println("=== Inizio test LoyaltyCard ===");
        LoyaltyCard card = new LoyaltyCard(1, 10, 5, 2);

        System.out.println("Card creata: id=" + card.getId() + ", userId=" + card.getUserId() +
                           ", totalBooking=" + card.getTotalBooking() + ", discountLevel=" + card.getDiscountLevel());

        assertEquals(1, card.getId());
        assertEquals(10, card.getUserId());
        assertEquals(5, card.getTotalBooking());
        assertEquals(2, card.getDiscountLevel());

        System.out.println("Aggiornamento valori totalBooking e discountLevel");
        card.setTotalBooking(7);
        card.setDiscountLevel(3);

        System.out.println("Valori aggiornati: totalBooking=" + card.getTotalBooking() + ", discountLevel=" + card.getDiscountLevel());
        assertEquals(7, card.getTotalBooking());
        assertEquals(3, card.getDiscountLevel());

        assertNotNull(card.totalBookingProperty());
        assertNotNull(card.discountLevelProperty());

        System.out.println("Test LoyaltyCard completato con successo");
    }
}

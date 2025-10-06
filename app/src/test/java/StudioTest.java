import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.model.Studio;

public class StudioTest {

    @Test
    void testGettersSettersAndToString() {
        System.out.println("=== Inizio test Studio ===");
        Studio s = new Studio(1, "Studio A", "Descrizione", true);

        System.out.println("Studio creato: id=" + s.getId() + ", name=" + s.getName() +
                           ", description=" + s.getDescription() + ", active=" + s.isActive());

        assertEquals(1, s.getId());
        assertEquals("Studio A", s.getName());
        assertEquals("Descrizione", s.getDescription());
        assertTrue(s.isActive());

        System.out.println("Aggiornamento valori name, description e active");
        s.setName("Studio B");
        s.setDescription("Nuova descrizione");
        s.setActive(false);

        System.out.println("Valori aggiornati: name=" + s.getName() + ", description=" + s.getDescription() +
                           ", active=" + s.isActive());

        assertEquals("Studio B", s.getName());
        assertEquals("Nuova descrizione", s.getDescription());
        assertFalse(s.isActive());

        System.out.println("Verifica toString(): " + s.toString());
        assertEquals("Studio B (Non Attivo)", s.toString());

        System.out.println("Test Studio completato con successo");
    }
}

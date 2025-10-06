import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.model.Equipment;

public class EquipmentTest {

    @Test
    void testGettersSettersAndToString() {
        System.out.println("=== Inizio test Equipment ===");

        Equipment e = new Equipment(1, "Microfono", "Condeser", true, "Audio");
        System.out.println("Creato Equipment: " + e);

        System.out.println("Verifica dei getter...");
        assertEquals(1, e.getId());
        assertEquals("Microfono", e.getName());
        assertEquals("Condeser", e.getDescription());
        assertTrue(e.isAvailable());
        assertEquals("Audio", e.getType());
        System.out.println("Getter verificati con successo!");

        System.out.println("Modifica dei valori tramite setter...");
        e.setName("Mic USB");
        e.setDescription("USB Condenser");
        e.setAvailable(false);
        e.setType("Audio");

        System.out.println("Valori aggiornati: " + e);
        assertEquals("Mic USB", e.getName());
        assertEquals("USB Condenser", e.getDescription());
        assertFalse(e.isAvailable());
        assertEquals("Audio", e.getType());
        System.out.println("Setter verificati con successo!");

        System.out.println("Verifica toString...");
        assertEquals("Mic USB", e.toString()); // visualizzazione in ListView
        System.out.println("toString verificato con successo!");

        System.out.println("=== Test Equipment completato con successo ===");
    }
}

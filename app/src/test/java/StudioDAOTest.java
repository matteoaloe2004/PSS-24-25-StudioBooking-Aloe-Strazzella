import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.studiobooking.dao.StudioDAO;
import com.example.studiobooking.model.Studio;

public class StudioDAOTest {

    private StudioDAO studioDAO;

    @BeforeEach
    void setup() {
        studioDAO = new StudioDAO();
        System.out.println("=== Inizio test StudioDAO ===");
    }

    @Test
    void testAddRetrieveUpdateStudio() {
        String uniqueName = "Studio Test " + System.currentTimeMillis();
        Studio studio = new Studio(0, uniqueName, "Descrizione Test", true);

        // Aggiungi studio
        boolean added = studioDAO.addStudio(studio);
        System.out.println("Studio aggiunto? " + added);
        assertTrue(added);

        // Recupera studio dall'elenco
        List<Studio> studios = studioDAO.getAllStudios();
        Studio retrieved = studios.stream()
                .filter(s -> s.getName().equals(uniqueName))
                .findFirst()
                .orElse(null);
        System.out.println("Studio recuperato: " + retrieved);
        assertNotNull(retrieved);

        // Aggiorna studio
        retrieved.setDescription("Nuova descrizione");
        boolean updated = studioDAO.updateStudio(retrieved);
        System.out.println("Studio aggiornato? " + updated);
        assertTrue(updated);

        Studio updatedStudio = studioDAO.getStudioById(retrieved.getId());
        System.out.println("Studio aggiornato recuperato: " + updatedStudio);
        assertEquals("Nuova descrizione", updatedStudio.getDescription());

        // Elimina studio
        boolean deleted = studioDAO.deleteStudio(updatedStudio.getId());
        System.out.println("PULIZIA: Studio eliminato? " + deleted);
        assertTrue(deleted);
    }
}

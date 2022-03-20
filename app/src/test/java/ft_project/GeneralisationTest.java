/*
 * Test case for the Generalisation class
 */
package ft_project;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class GeneralisationTest {
    private Generalisation g;

    @BeforeEach
    public void beforeEach() {
        g = new Generalisation();
    }

    @Test
    public void testInfoLoss() {
        assertEquals(g.infoLoss(), 0);
    }

    @Test
    public void testUpdateGeneralisation() {
        assertFalse(g.updateGeneralisation());
    }

    @Test
    public void testUpdateGeneralisationString() {
        assertFalse(g.updateGeneralisation("test"));
    }

    @Test
    public void testUpdateGeneralisationFloat() {
        assertFalse(g.updateGeneralisation((float) 1.1));
    }

    @Test
    public void testClone() {
        try {
            assertNotEquals(g, g.clone());
        } catch (CloneNotSupportedException e) {
            fail("unexpected clone not supported exception");
        }
    }
}

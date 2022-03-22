/*
 * Test case for the CategoryGeneralisation class
 */
package ft_project;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CategoryGeneralisationTest {
    DGH d;
    CategoryGeneralisation g;

    @BeforeEach
    public void beforeEach() {
        d = new DGH("location");
        d.add("west midlands");
        d.add("coventry");
        d.add("birmingham");
        g = new CategoryGeneralisation(d, "coventry");
    }

    @Test
    public void testInfoLoss() {
        g = new CategoryGeneralisation(d, "west midlands");
        assertEquals(g.infoLoss(), 1.0);
    }

    @Test
    public void testUpdateGeneralisation() {
        assertTrue(g.updateGeneralisation("birmingham"));
        assertTrue(g.toString().contains("west midlands"));
    }

    @Test
    public void testToString() {
        assertEquals("coventry", g.toString());
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

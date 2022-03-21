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
        d.add("location", "west midlands");
        d.add("west midlands", "coventry");
        d.add("west midlands", "birmingham");
        g = new CategoryGeneralisation(d, "coventry");
    }

    // TODO test info loss

    // TODO test updateGeneralisation

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

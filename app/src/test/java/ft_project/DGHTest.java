/*
 * Test case for the DGH class
 */
package ft_project;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DGHTest {
    DGH d;

    @BeforeEach
    public void beforeEach() {
        d = new DGH("location");
    }

    @Test
    public void testAdd() {
        assertTrue(d.add("west midlands"));
    }

    @Test
    public void testContains() {
        d.add("west midlands");
        assertTrue(d.contains("west midlands"));
    }

    @Test
    public void testFind() {
        d.add("west midlands");
        assertNotNull(d.find("west midlands"));
    }

    @Test
    public void testCountNodes() {
        assertEquals(0, d.countNodes());
    }

    @Test
    public void testFindCommonAncestor() {
        d.add("west midlands");
        d.add("coventry");
        d.add("birmingham");
        assertTrue(d.findCommonAncestor("coventry", "birmingham").toString().contains("west midlands"));
    }

    @Test
    public void testGetRootValue() {
        d.add("west midlands");
        assertTrue(d.getRootValue().contains("west midlands"));
    }

    @Test
    public void testToString() {
        d.add("west midlands");
        assertTrue(d.toString().contains("west midlands"));
    }
}

/*
 * Test case for the Tuple class
 */
package ft_project;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class TupleTest {
    private Tuple t;
    private String[] headings = { "pid", "location" };
    private String[] data = { "1", "coventry" };

    @BeforeEach
    public void beforeEach() {
        // test setup code
        t = new Tuple(headings, data);
    }

    @Test
    public void testTrue() {
        assertTrue(true);
    }

    // TODO test suppress

    @Test
    public void testGetPID() {
        assertEquals(t.getPid(), "1");
    }

    @Test
    public void testGetValueIndex() {
        assertEquals(t.getValue(1), "coventry");
    }

    @Test
    public void testGetValueString() {
        assertEquals(t.getValue("location"), "coventry");
    }

    @Test
    public void testHasBeenOutput() {
        assertFalse(t.hasBeenOutput());
    }

    @Test
    public void setAsBeenOutput() {
        t.setAsBeenOutput();
        assertTrue(t.hasBeenOutput());
    }

    @Test
    public void testToString() {
        assertEquals(t.toString(), "1 coventry ");
    }

    @Test
    public void testClone() {
        try {
            assertNotEquals(t, (Tuple) t.clone());
        } catch (CloneNotSupportedException e) {
            fail("clone not supported exception not expected");
        }
    }
}

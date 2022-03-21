/*
 * Test case for the ContinuousGeneralisation class
 */
package ft_project;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ContinuousGeneralisationTest {
    private int LB = 0, UB = 100, lb = 20, ub = 60;
    private ContinuousGeneralisation g;

    @BeforeEach
    public void beforeEach() {
        g = new ContinuousGeneralisation(LB, UB, lb, ub);
    }

    @Test
    void testConstructorTwoArgs() {
        g = new ContinuousGeneralisation(LB, UB);

        assertEquals(g.UB, UB);
        assertEquals(g.LB, LB);
        assertEquals(g.ub, UB);
        assertEquals(g.lb, LB);
    }

    @Test
    void testConstructorOneArgs() {
        g = new ContinuousGeneralisation(100);

        assertEquals(g.UB, 9999999);
        assertEquals(g.LB, 0);
        assertEquals(g.ub, 100);
        assertEquals(g.lb, 100);
    }

    @Test
    public void testGetMaxGeneralisation() {
        assertEquals("[ " + Float.toString(LB) + " " + Float.toString(UB) + " ]", g.getMaxGeneralisation());
    }

    @Test
    public void testInfoLoss() {
        assertEquals(g.infoLoss(), (ub - lb) / (float) (UB - LB));
    }

    @Test
    public void testUpdateGeneralisationTrue() {
        assertTrue(g.updateGeneralisation(10, 90));
        assertEquals(10, g.lb);
        assertEquals(90, g.ub);
    }

    @Test
    public void testUpdateGeneralisationFalse() {
        assertFalse(g.updateGeneralisation(-10, 200));
    }

    @Test
    public void testUpdateGeneralisationString() {
        assertFalse(g.updateGeneralisation("test"));
    }

    @Test
    public void testUpdateGeneralisationFloat() {
        assertTrue(g.updateGeneralisation((float) 5));
        assertEquals(5, g.lb);
    }

    @Test
    public void testToString() {
        assertEquals("[ " + Float.toString(lb) + " " + Float.toString(ub) + " ]", g.toString());
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

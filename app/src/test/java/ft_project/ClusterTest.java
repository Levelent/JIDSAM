/*
 * Test case for the Cluster class
 */
package ft_project;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class ClusterTest {
    private String[] headings = { "pid", "salary" };
    private String[] data = { "1", "100" };

    private Tuple t;
    private Map<String, DGH> d;
    private Cluster c;

    @BeforeEach
    public void beforeEach() {
        t = new Tuple(headings, data);
        d = new HashMap<>();
        c = new Cluster(t, d);
    }

    @Test
    public void testMerge() {
        String[] headings = { "pid", "salary" };
        String[] data = { "2", "200" };

        Tuple t2 = new Tuple(headings, data);
        Cluster c2 = new Cluster(t2, d);

        c.merge(c2);
        assertEquals(c.size(), 2);
    }

    @Test
    public void testDistinctValues() {
        assertEquals(c.distinctValues(1).iterator().next(), "100");
    }

    @Test
    public void testDiversity() {
        assertEquals(c.diversity(1), 1);
    }

    @Test
    public void testContainsTrue() {
        assertTrue(c.contains(t));
    }

    @Test
    public void testContainsFalse() {
        assertFalse(c.contains(new Tuple(headings, data)));
    }

    @Test
    public void testAdd() {
        String[] headings = { "pid", "salary" };
        String[] data = { "2", "200" };

        c.add(new Tuple(headings, data));
        assertEquals(c.size(), 2);
    }

    @Test
    public void testAddCollection() {
        String[] headings = { "pid", "salary" };
        String[] data = { "2", "200" };

        List<Tuple> s = new LinkedList<>();
        s.add(new Tuple(headings, data));
        c.add(s);
        assertEquals(c.size(), 2);
    }

    // TODO information loss

    @Test
    public void testRemoveSet() {
        Set<Tuple> s = new LinkedHashSet<>();
        s.add(t);

        c.removeSet(s);
        assertEquals(c.size(), 0);
    }

    @Test
    public void testSize() {
        assertEquals(c.size(), 1);
    }

    @Test
    public void testGetTuples() {
        assertEquals(c.getTuples().get(0), t);
    }

    // TODO getGeneralisation

    @Test
    public void testGetDGH() {
        assertEquals(d, c.getDGH());
    }

    @Test
    public void testClone() {
        try {
            assertNotEquals(c, c.clone());
        } catch (CloneNotSupportedException e) {
            fail("unexpected clone not supported exception");
        }
    }
}

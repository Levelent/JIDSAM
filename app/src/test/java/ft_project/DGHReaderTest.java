/*
 * Test case for the DGHReader class
 */
package ft_project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DGHReaderTest {
    // TODO constructor

    @Test
    public void testIndentCount() {
        assertEquals(DGHReader.indentCount("        "), 2);
    }
}

/*
 * Test case for the DGHReader class
 */
package ft_project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class DGHReaderTest {
    @Test
    public void testInStream() {
        try {
            // create temp file
            File temp = File.createTempFile("temp", ".txt");
            FileWriter writer = new FileWriter(temp);
            writer.write(
                    "$location" + System.lineSeparator() + "west midlands" + System.lineSeparator() + "    coventry"
                            + System.lineSeparator() + "    birmingham");
            writer.close();

            // read DGH
            DGHReader reader = new DGHReader(temp.getAbsolutePath());

            // test dgh
            DGH location = reader.DGHs.get("location");
            assertNotNull(location);
            assertEquals("west midlands", location.getRootValue());
            assertNotNull(location.find("coventry"));
        } catch (IOException e) {
            fail("IO Exception not expected");
        }
    }

    @Test
    public void testIndentCount() {
        assertEquals(DGHReader.indentCount("        "), 2);
    }
}

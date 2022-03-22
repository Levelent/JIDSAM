/*
 * Test case for the DGHReader class
 */
package ft_project;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class DGHReaderTest {
    static Map<String, DGH> DghMap;

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

    @BeforeEach
    public void beforeEach() {
        DGHReader DGHr = new DGHReader(".src/test/resources/dgh-indents");
        DghMap = DGHr.DGHs;
    }

    @Test
    public void testIndents() {
        DGH indents = DghMap.get("dgh-indents");

        assertTrue(indents.countNodes("1") == 1);
        assertTrue(indents.countNodes("1.1") == 6);
        assertTrue(indents.countNodes("1.2") == 1);
        assertTrue(indents.countNodes("1.3") == 7);
        assertTrue(indents.countNodes("1.4") == 1);

    }

    @Test
    public void testNames() {
        assertAll(
                () -> {
                    DGH dgh = DghMap.get("name-test1");
                    assertTrue(dgh != null);
                    assertTrue(dgh.countNodes() == 2);
                },
                () -> {
                    DGH dgh = DghMap.get("name-test2");
                    assertTrue(dgh != null);
                    assertTrue(dgh.countNodes() == 4);
                },
                () -> {
                    DGH dgh = DghMap.get("name-test3");
                    assertTrue(dgh != null);
                    assertTrue(dgh.countNodes() == 1);
                });
    }
}

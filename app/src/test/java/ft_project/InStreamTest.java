/*
 * Test case for the InStream class
 */
package ft_project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class InStreamTest {
    @Test
    public void testInStream() {
        try {
            // create temp file
            File temp = File.createTempFile("temp", ".txt");
            FileWriter writer = new FileWriter(temp);
            writer.write("pid,location" + System.lineSeparator());
            writer.write("1,coventry" + System.lineSeparator());
            writer.close();

            // create in stream and get tuple
            InStream in = new InStream(temp.getAbsolutePath());
            Tuple t = in.next();

            // check tuple generated
            String[] expectedHeadings = { "pid", "location" };
            assertArrayEquals(t.headings, expectedHeadings);
            assertEquals(t.getPid(), "1");

            // check if no tuple then null
            assertNull(in.next());

            // clean up
            in.close();
        } catch (IOException e) {
            fail("IO Exception not expected");
        }
    }
}

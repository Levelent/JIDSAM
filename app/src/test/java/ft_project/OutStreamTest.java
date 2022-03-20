/*
 * Test case for the OutStream class
 */
package ft_project;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

class OutStreamTest {
    @Test
    public void testStream() {
        try {
            File temp = File.createTempFile("temp", ".txt");
            OutStream outStream = new OutStream(temp.getAbsolutePath());
            outStream.out.println("test");
            outStream.close();

            // test added to file
            Scanner myReader = new Scanner(temp);
            if (myReader.hasNextLine()) {
                assertEquals(myReader.nextLine(), "test");
            } else {
                fail("file not written to");
            }
            myReader.close();
        } catch (IOException e) {
            fail("IO Exception not expected");
        }
    }
}

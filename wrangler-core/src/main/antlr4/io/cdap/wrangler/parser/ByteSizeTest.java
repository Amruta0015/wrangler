package io.cdap.wrangler.api.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class ByteSizeTest {
    @Test
    public void testByteSizeParsing() {
        assertEquals(1024, new ByteSize("1KB").getBytes());
        assertEquals(1024 * 1024, new ByteSize("1MB").getBytes());
        assertEquals(1.5 * 1024 * 1024, new ByteSize("1.5MB").getBytes(), 0.001);
        assertEquals(1024, new ByteSize("1kb").getBytes());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteSize() {
        new ByteSize("1XB");
    }
}

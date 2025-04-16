package io.cdap.wrangler.api.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimeDurationTest {
    @Test
    public void testTimeDurationParsing() {
        assertEquals(1_000_000, new TimeDuration("1ms").getNanoseconds());
        assertEquals(1_000_000_000, new TimeDuration("1s").getNanoseconds());
        assertEquals(1.5 * 1_000_000_000, new TimeDuration("1.5s").getNanoseconds(), 0.001);
        assertEquals(60 * 1_000_000_000L, new TimeDuration("1m").getNanoseconds());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        new TimeDuration("1xs");
    }
}

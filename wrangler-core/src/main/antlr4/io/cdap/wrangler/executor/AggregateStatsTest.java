package io.cdap.wrangler.executor;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.executor.AggregateStats;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AggregateStatsTest {
    @Test
    public void testAggregateStats() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", "1KB").add("response_time", "100ms"),
            new Row("data_transfer_size", "2KB").add("response_time", "200ms"),
            new Row("data_transfer_size", "0.5KB").add("response_time", "50ms")
        );
        
        AggregateStats directive = new AggregateStats();
        directive.initialize(new TestArguments(
            "size-column", "data_transfer_size",
            "time-column", "response_time",
            "output-size-column", "total_size_mb",
            "output-time-column", "total_time_sec"
        ));
        
        directive.execute(rows, null);
        List<Row> result = directive.finalize();
        
        assertEquals(1, result.size());
        assertEquals(3.5 * 1024 / (1024 * 1024), (double) result.get(0).getValue("total_size_mb"), 0.001);
        assertEquals(0.350, (double) result.get(0).getValue("total_time_sec"), 0.001);
    }
    
    @Test
    public void testAverageAggregateStats() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("size", "1KB").add("time", "100ms"),
            new Row("size", "2KB").add("time", "200ms"),
            new Row("size", "1KB").add("time", "100ms")
        );
        
        AggregateStats directive = new AggregateStats();
        directive.initialize(new TestArguments(
            "size-column", "size",
            "time-column", "time",
            "output-size-column", "avg_size_kb",
            "output-time-column", "avg_time_ms",
            "size-unit", "KB",
            "time-unit", "ms",
            "agg-type", "average"
        ));
        
        directive.execute(rows, null);
        List<Row> result = directive.finalize();
        
        assertEquals(1, result.size());
        assertEquals((1 + 2 + 1) / 3.0, (double) result.get(0).getValue("avg_size_kb"), 0.001);
        assertEquals((100 + 200 + 100) / 3.0, (double) result.get(0).getValue("avg_time_ms"), 0.001);
    }
    
    // Helper class for testing
    private static class TestArguments implements Arguments {
        private final String[] keys;
        private final Object[] values;
        
        TestArguments(Object... keyValues) {
            this.keys = new String[keyValues.length / 2];
            this.values = new Object[keyValues.length / 2];
            
            for (int i = 0; i < keyValues.length; i += 2) {
                keys[i / 2] = (String) keyValues[i];
                values[i / 2] = keyValues[i + 1];
            }
        }
        
        @Override
        public <T extends Token> T value(String name) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i].equals(name)) {
                    if (values[i] instanceof String) {
                        if (name.contains("column")) {
                            return (T) new ColumnName((String) values[i]);
                        } else {
                            return (T) new Identifier((String) values[i]);
                        }
                    }
                }
            }
            return null;
        }
        
        @Override
        public boolean contains(String name) {
            for (String key : keys) {
                if (key.equals(name)) return true;
            }
            return false;
        }
    }
}
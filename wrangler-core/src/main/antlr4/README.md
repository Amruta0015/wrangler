## Byte Size and Time Duration Parsers

Wrangler now supports parsing byte sizes and time durations with units:

### Byte Sizes
- Supported units: B, KB, MB, GB, TB (case insensitive)
- Examples: "10KB", "1.5MB", "2tb"
- The values are converted to bytes internally

### Time Durations
- Supported units: ns, us, ms, s, m, h, d (case insensitive)
- Examples: "100ms", "1.5s", "2h"
- The values are converted to nanoseconds internally

### Aggregate Stats Directive
The `aggregate-stats` directive aggregates byte size and time duration columns:

```wrangler
aggregate-stats :size_column :time_column :output_size_column :output_time_column [size_unit] [time_unit] [agg_type]
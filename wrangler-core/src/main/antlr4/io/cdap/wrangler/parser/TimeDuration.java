public class TimeDuration extends Token {
    private final long nanoseconds;

    public TimeDuration(String value) {
        super(TokenType.TIME_DURATION, value);
        this.nanoseconds = parseTimeDuration(value);
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    public long getMilliseconds() {
        return nanoseconds / 1_000_000;
    }

    public long getSeconds() {
        return nanoseconds / 1_000_000_000;
    }

    private long parseTimeDuration(String str) {
        // Implementation to parse "10ms", "1.5s" etc. into nanoseconds
        String numStr = str.replaceAll("[^0-9.]", "");
        String unit = str.replaceAll("[0-9.\\s]", "").toLowerCase();
        
        double num = Double.parseDouble(numStr);
        
        switch (unit) {
            case "ns": return (long) num;
            case "us": return (long) (num * 1_000);
            case "ms": return (long) (num * 1_000_000);
            case "s": return (long) (num * 1_000_000_000);
            case "m": return (long) (num * 60 * 1_000_000_000L);
            case "h": return (long) (num * 60 * 60 * 1_000_000_000L);
            case "d": return (long) (num * 24 * 60 * 60 * 1_000_000_000L);
            default: throw new IllegalArgumentException("Invalid time unit: " + unit);
        }
    }
}
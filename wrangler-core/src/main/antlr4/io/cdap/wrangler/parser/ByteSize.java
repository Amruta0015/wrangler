public class ByteSize extends Token {
    private final long bytes;

    public ByteSize(String value) {
        super(TokenType.BYTE_SIZE, value);
        this.bytes = parseByteSize(value);
    }

    public long getBytes() {
        return bytes;
    }

    private long parseByteSize(String str) {
        // Implementation to parse "10KB", "1.5MB" etc. into bytes
        String numStr = str.replaceAll("[^0-9.]", "");
        String unit = str.replaceAll("[0-9.\\s]", "").toUpperCase();
        
        double num = Double.parseDouble(numStr);
        
        switch (unit) {
            case "B": return (long) num;
            case "KB": return (long) (num * 1024);
            case "MB": return (long) (num * 1024 * 1024);
            case "GB": return (long) (num * 1024 * 1024 * 1024);
            case "TB": return (long) (num * 1024 * 1024 * 1024 * 1024);
            default: throw new IllegalArgumentException("Invalid byte size unit: " + unit);
        }
    }
}
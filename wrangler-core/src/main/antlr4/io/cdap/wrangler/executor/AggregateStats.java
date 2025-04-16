import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.parser.*;
import io.cdap.wrangler.expression.EL;
import io.cdap.wrangler.expression.ELContext;
import io.cdap.wrangler.expression.ELException;
import io.cdap.wrangler.expression.ELResult;

import java.util.ArrayList;
import java.util.List;

@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Description("Aggregates byte size and time duration columns")
public class AggregateStats implements Directive, AggregateInterpreter {
    private String sizeColumn;
    private String timeColumn;
    private String outputSizeColumn;
    private String outputTimeColumn;
    private String sizeUnit = "MB";
    private String timeUnit = "s";
    private String aggType = "total";
    
    private long totalBytes = 0;
    private long totalNanos = 0;
    private int rowCount = 0;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("size-column", TokenType.COLUMN_NAME);
        builder.define("time-column", TokenType.COLUMN_NAME);
        builder.define("output-size-column", TokenType.COLUMN_NAME);
        builder.define("output-time-column", TokenType.COLUMN_NAME);
        builder.define("size-unit", TokenType.IDENTIFIER, Optionality.OPTIONAL);
        builder.define("time-unit", TokenType.IDENTIFIER, Optionality.OPTIONAL);
        builder.define("agg-type", TokenType.IDENTIFIER, Optionality.OPTIONAL);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeColumn = ((ColumnName) args.value("size-column")).value();
        this.timeColumn = ((ColumnName) args.value("time-column")).value();
        this.outputSizeColumn = ((ColumnName) args.value("output-size-column")).value();
        this.outputTimeColumn = ((ColumnName) args.value("output-time-column")).value();
        
        if (args.contains("size-unit")) {
            this.sizeUnit = ((Identifier) args.value("size-unit")).value();
        }
        if (args.contains("time-unit")) {
            this.timeUnit = ((Identifier) args.value("time-unit")).value();
        }
        if (args.contains("agg-type")) {
            this.aggType = ((Identifier) args.value("agg-type")).value();
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        for (Row row : rows) {
            int sizeIdx = row.find(sizeColumn);
            int timeIdx = row.find(timeColumn);
            
            if (sizeIdx != -1) {
                Object sizeVal = row.getValue(sizeIdx);
                if (sizeVal instanceof String) {
                    ByteSize bs = new ByteSize((String) sizeVal);
                    totalBytes += bs.getBytes();
                }
            }
            
            if (timeIdx != -1) {
                Object timeVal = row.getValue(timeIdx);
                if (timeVal instanceof String) {
                    TimeDuration td = new TimeDuration((String) timeVal);
                    totalNanos += td.getNanoseconds();
                }
            }
            
            rowCount++;
        }
        
        return new ArrayList<>();
    }

    @Override
    public List<Row> finalize() throws DirectiveExecutionException {
        Row row = new Row();
        
        // Calculate size in requested unit
        double outputSize;
        switch (sizeUnit.toUpperCase()) {
            case "B": outputSize = totalBytes; break;
            case "KB": outputSize = totalBytes / 1024.0; break;
            case "MB": outputSize = totalBytes / (1024.0 * 1024.0); break;
            case "GB": outputSize = totalBytes / (1024.0 * 1024.0 * 1024.0); break;
            case "TB": outputSize = totalBytes / (1024.0 * 1024.0 * 1024.0 * 1024.0); break;
            default: throw new DirectiveExecutionException("Invalid size unit: " + sizeUnit);
        }
        
        // Calculate time in requested unit
        double outputTime;
        switch (timeUnit.toLowerCase()) {
            case "ns": outputTime = totalNanos; break;
            case "us": outputTime = totalNanos / 1_000.0; break;
            case "ms": outputTime = totalNanos / 1_000_000.0; break;
            case "s": outputTime = totalNanos / 1_000_000_000.0; break;
            case "m": outputTime = totalNanos / (60.0 * 1_000_000_000.0); break;
            case "h": outputTime = totalNanos / (60.0 * 60.0 * 1_000_000_000.0); break;
            case "d": outputTime = totalNanos / (24.0 * 60.0 * 60.0 * 1_000_000_000.0); break;
            default: throw new DirectiveExecutionException("Invalid time unit: " + timeUnit);
        }
        
        // Apply aggregation type
        if ("average".equalsIgnoreCase(aggType)) {
            outputSize = rowCount > 0 ? outputSize / rowCount : 0;
            outputTime = rowCount > 0 ? outputTime / rowCount : 0;
        }
        
        row.add(outputSizeColumn, outputSize);
        row.add(outputTimeColumn, outputTime);
        
        List<Row> result = new ArrayList<>();
        result.add(row);
        return result;
    }

    @Override
    public void destroy() {
        // Clean up if needed
    }
}

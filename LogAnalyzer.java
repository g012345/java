import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class LogAnalyzer{

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java LogAnalyzer <log-file-path>");
            return;
        }

        String logFilePath = args[0];
        Path path = Paths.get(logFilePath);

        if (!Files.exists(path)) {
            System.err.println("Error: Log file not found");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            analyzeLogs(reader);
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
        }
    }

    private static void analyzeLogs(BufferedReader reader) throws IOException {
        Map<String, Integer> resourceRequests = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        int totalRequests = 0;
        long totalBytes = 0;

        String line;
        while ((line = reader.readLine()) != null) {
            LogEntry logEntry = parseLogEntry(line);

            if (logEntry != null) {
                totalRequests++;
                totalBytes += logEntry.getBytesSent();

                // Update resource requests map
                String resource = logEntry.getResource();
                resourceRequests.put(resource, resourceRequests.getOrDefault(resource, 0) + 1);

                // Update response codes map
                int statusCode = logEntry.getStatusCode();
                responseCodes.put(statusCode, responseCodes.getOrDefault(statusCode, 0) + 1);
            }
        }

        // Output results
        System.out.println("# Общая информация");
        System.out.println();
        System.out.println("|  Метрика            |    значение   |");
        System.out.println("|---------------------|---------------|");
        System.out.println("| количество запросов |    " + totalRequests + "     |");
        
        long averageResponseSize = (totalRequests != 0) ? (totalBytes / totalRequests) : 0;
        System.out.println("| средний размер ответа|    " + averageResponseSize + "b       |");
        System.out.println();

        // Output resource requests
        System.out.println("запрашиваемый ресурс ");
        System.out.println();
        System.out.println("|   Ресурс         |   Количество    |");
        for (Map.Entry<String, Integer> entry : resourceRequests.entrySet()) {
            System.out.println("| \"" + entry.getKey() + "\"     |  " + entry.getValue() + "          |");
        }
        System.out.println();

        // Output response codes
        System.out.println("код ответа ");
        System.out.println();
        System.out.println("|код   |      имя              | колво   |");
        for (Map.Entry<Integer, Integer> entry : responseCodes.entrySet()) {
            System.out.println("|" + entry.getKey() + "   |     ...               | " + entry.getValue() + "     |");
        }
    }

    private static LogEntry parseLogEntry(String logLine) {
        String[] parts = logLine.split(" ");
        if (parts.length >= 12) {
            String timestamp = parts[3] + " " + parts[4];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[dd/MMM/yyyy:HH:mm:ss Z]");
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);

            int statusCode = Integer.parseInt(parts[parts.length - 2]);
            long bytesSent = Long.parseLong(parts[parts.length - 1]);

            String resource = parts[6];

            return new LogEntry(dateTime, resource, statusCode, bytesSent);
        }
        return null;
    }

    private static class LogEntry {
        private LocalDateTime timestamp;
        private String resource;
        private int statusCode;
        private long bytesSent;

        public LogEntry(LocalDateTime timestamp, String resource, int statusCode, long bytesSent) {
            this.timestamp = timestamp;
            this.resource = resource;
            this.statusCode = statusCode;
            this.bytesSent = bytesSent;
        }

        public String getResource() {
            return resource;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public long getBytesSent() {
            return bytesSent;
        }
    }
}

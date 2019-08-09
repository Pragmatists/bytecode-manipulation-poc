package demo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimePrinter {
    void now() {
        long currentTimeMillis = System.currentTimeMillis();
        LocalDateTime localDateTime =
                Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.print(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME) + " ");
    }
}

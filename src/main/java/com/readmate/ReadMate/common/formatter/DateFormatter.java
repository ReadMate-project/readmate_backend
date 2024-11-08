package com.readmate.ReadMate.common.formatter;

import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Component
public class DateFormatter {

    public static String formatDate(final LocalDateTime date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        return date.format(formatter);
    }
}

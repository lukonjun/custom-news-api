package org.comppress.customnewsapi.utils;

import org.comppress.customnewsapi.exceptions.DateFormatException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static LocalDateTime stringToLocalDateTime(String time){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = null;
        if (time != null){
            try {
                dateTime = LocalDateTime.parse(time, formatter);
            }catch (Exception e){
                throw new DateFormatException("Date format is wrong", "correct format is yyyy-MM-dd HH:mm:ss");
            }
        }
        return dateTime;
    }

}

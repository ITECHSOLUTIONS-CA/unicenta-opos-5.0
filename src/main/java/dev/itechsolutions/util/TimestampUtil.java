/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

/**
 *
 * @author Argenis Rodríguez
 */
public class TimestampUtil {
    
    public static Date now() {
        long time = Timestamp.valueOf(LocalDate.now().atStartOfDay()).getTime();
        return new Date(time);
    }
    
    public static Date trunc(Date date, TemporalUnit unit) {
        
        if (date == null || unit == null)
            return null;
        
        Timestamp tsDate = new Timestamp(date.getTime());
        LocalDateTime dt = tsDate.toLocalDateTime()
                .truncatedTo(unit);
        
        long time = Timestamp.valueOf(dt).getTime();
        return new Date(time);
    }
    
    public static Date truncDay(Date date) {
        return trunc(date, ChronoUnit.DAYS);
    }
}

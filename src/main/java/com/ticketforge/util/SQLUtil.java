package com.ticketforge.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SQLUtil {
    private SQLUtil(){}

    public static Timestamp toTimeStamp(LocalDateTime value){
        return value==null?null:Timestamp.valueOf(value);
    }
    public  static LocalDateTime toLocalDateTime(Timestamp value){
        return value==null?null:value.toLocalDateTime();
    }
}

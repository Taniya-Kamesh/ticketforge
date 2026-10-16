package com.ticketforge.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {
    public static Connection getConnection() throws Exception{
        Properties prop=new Properties();
        try(InputStream in=DBConnection.class.getClassLoader().getResourceAsStream("db.properties")){
           if(in==null){
               throw new RuntimeException("Couldn't find src/main/resources/db.properties");
           }
           prop.load(in);
        }
        return DriverManager.getConnection(
        prop.getProperty("db.url"),
        prop.getProperty("db.user"),
        prop.getProperty("db.password"));
    }
}

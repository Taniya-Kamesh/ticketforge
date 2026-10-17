package com.ticketforge.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    public static Connection getConnection() throws SQLException {
        Properties prop=new Properties();
        try(InputStream in=DBConnection.class.getClassLoader().getResourceAsStream("db.properties")){
           if(in==null){
               throw new RuntimeException("Couldn't find src/main/resources/db.properties");
           }
           prop.load(in);
        }catch (IOException e){
            throw new SQLException("Could not read db.properties", e);
        }
        return DriverManager.getConnection(
        prop.getProperty("db.url"),
        prop.getProperty("db.user"),
        prop.getProperty("db.password"));
    }
}

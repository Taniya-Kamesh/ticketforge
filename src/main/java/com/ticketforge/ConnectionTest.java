package com.ticketforge;

import com.ticketforge.util.DBConnection;

import java.sql.Connection;

public class ConnectionTest {
    public static void main(String[] args) {
        try(Connection con= DBConnection.getConnection()){
           System.out.println("Connected successfully");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

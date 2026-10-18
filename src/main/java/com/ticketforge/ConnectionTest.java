package com.ticketforge;

import com.ticketforge.dao.EventDAO;
import com.ticketforge.dao.SeatDAO;
import com.ticketforge.dao.UserDAO;
import com.ticketforge.model.*;
import com.ticketforge.util.DBConnection;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class ConnectionTest{
    public static void main(String[] args) {
      try {
          EventDAO eventDAO = new EventDAO();
          SeatDAO seatDAO = new SeatDAO();
          UserDAO userDAO = new UserDAO();

          List<Event> events = eventDAO.findAll();
          events.forEach(System.out::println);

          if (!events.isEmpty()) {
              long eventId = events.get(0).getId();
              System.out.println("--- Seats for event " + eventId + " ---");
              seatDAO.findByEvent(eventId).forEach(System.out::println);
          }

          // 2. Insert a user, then read it back
          User user = new User();
          user.setName("Test User");
          user.setEmail("test" + System.currentTimeMillis() + "@example.com");
          user.setPasswordHash("not-a-real-hash");
          user.setRole(Role.USER);
          userDAO.insert(user);
          System.out.println("--- Inserted ---");
          System.out.println(user);

          System.out.println("--- Found by email ---");
          System.out.println(userDAO.findByEmail(user.getEmail()).orElse(null));

      } catch (Exception e) {
          e.printStackTrace();
      }
    }
}

   




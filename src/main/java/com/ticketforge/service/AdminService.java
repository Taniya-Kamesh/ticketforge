package com.ticketforge.service;

import com.ticketforge.dao.EventDAO;
import com.ticketforge.dao.SeatDAO;
import com.ticketforge.exception.NotAuthorizedException;
import com.ticketforge.exception.ValidationException;
import com.ticketforge.model.*;
import com.ticketforge.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AdminService {
    private final EventDAO eventDAO=new EventDAO();
    private final SeatDAO seatDAO=new SeatDAO();

    public Event createEventWithSeats(User admin, String title, String venue, LocalDateTime startAt,
                                      int rows, int seatsPerRow)
            throws NotAuthorizedException, ValidationException, SQLException {
        if (admin == null || admin.getRole() != Role.ADMIN) {
            throw new NotAuthorizedException("Only admins can create events");
        }
        if (title == null || title.trim().isEmpty() || title.trim().length() > 100) {
            throw new ValidationException("Title is required (max 100 characters)");
        }
        if (venue == null || venue.trim().isEmpty() || venue.trim().length() > 100) {
            throw new ValidationException("Venue is required (max 100 characters)");
        }
        if (startAt == null || startAt.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start time must be in the future");
        }
        if (rows < 1 || rows > 26) {
            throw new ValidationException("Rows must be between 1 and 26");
        }
        if (seatsPerRow < 1 || seatsPerRow > 100) {
            throw new ValidationException("Seats per row must be between 1 and 100");
        }

        try(Connection con= DBConnection.getConnection()){
            con.setAutoCommit(false);
            try{
                Event event=new Event();
                event.setTitle(title.trim());
                event.setVenue(venue.trim());
                event.setStartsAt(startAt);
                eventDAO.insert(event);
                for(int r=0;r<rows;r++){
                    char rowLetter=(char)('A'+r);
                    for(int n=1;n<=seatsPerRow;n++ ){
                      Seat seat=new Seat();
                      seat.setEventId(event.getId());
                      seat.setSeatLabel(String.valueOf(rowLetter) + n);
                      seat.setStatus(SeatStatus.AVAILABLE);
                      seatDAO.insert(con, seat);
                    }
                }
                con.commit();
                return event;
            }catch(SQLException | RuntimeException e){
                con.rollback();
                throw e;
            }
        }

    }
}

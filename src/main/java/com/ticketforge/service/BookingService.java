package com.ticketforge.service;

import com.ticketforge.dao.BookingDAO;
import com.ticketforge.dao.SeatDAO;
import com.ticketforge.model.Booking;
import com.ticketforge.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class BookingService {

    public static final int HOLD_SECONDS=300;
    public static final SeatDAO seatDAO=new SeatDAO();
    public static final BookingDAO bookingDAO=new BookingDAO();

    public boolean holdSeat(long seatId,long userId) throws SQLException {
        try(Connection con= DBConnection.getConnection()){
           return seatDAO.tryHold(con,seatId,userId,HOLD_SECONDS);
        }
    }
    public Booking confirmBooking(long seatId, long userId) throws SQLException{
        try(Connection con=DBConnection.getConnection()){
            con.setAutoCommit(false);
            try {
                if (!seatDAO.confirmHold(con, seatId, userId)) {
                    throw new IllegalStateException( "Seat " + seatId + " is not held by user " + userId + " (or the hold expired)");

                }
                Booking booking=new Booking();
                booking.setSeatId(seatId);
                booking.setUserId(userId);
                bookingDAO.insert(con,booking);
                con.commit();
                return booking;

            }catch(SQLException | RuntimeException e){
              con.rollback();
             throw e;
            }
        }
    }

    public int  releaseExpiredHolds() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return seatDAO.releaseExpiredHolds(con);
        }
    }
}

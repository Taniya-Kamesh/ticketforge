package com.ticketforge.dao;

import com.ticketforge.model.Booking;
import com.ticketforge.util.DBConnection;
import com.ticketforge.util.SQLUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    public Booking insert(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (user_id, seat_id) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, booking.getUserId());
            ps.setLong(2, booking.getSeatId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    booking.setId(keys.getLong(1));
                }
            }
        }
        return booking;
    }

    public List<Booking> findByUser(long id) throws SQLException{
        String sql=""" 
                  SELECT id, user_id, seat_id, booked_at FROM bookings
                  WHERE user_id = ? ORDER BY booked_at DESC""";

        List<Booking> bookings=new ArrayList<>();

        try(Connection con=DBConnection.getConnection();
        PreparedStatement ps=con.prepareStatement(sql)
        ){
            ps.setLong(1,id);

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Booking book=new Booking();
                    book.setId(rs.getLong("id"));
                    book.setUserId(rs.getLong("user_id"));
                    book.setSeatId(rs.getLong("seat_id"));
                    book.setBookedAt(SQLUtil.toLocalDateTime(rs.getTimestamp("booked_at")));
                    bookings.add(book);
                }
            }
        } return bookings;

    }

}

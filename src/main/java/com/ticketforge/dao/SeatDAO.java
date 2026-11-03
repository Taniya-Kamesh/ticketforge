package com.ticketforge.dao;

import com.ticketforge.model.Seat;
import com.ticketforge.model.SeatStatus;
import com.ticketforge.util.DBConnection;
import com.ticketforge.util.SQLUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeatDAO {

    public static final String COLUMNS="id,event_id,seat_label,status,held_by,hold_expires_at";

    public Seat insert(Seat seat) throws SQLException{
        try(Connection con= DBConnection.getConnection()){
            return insert(con,seat);
        }
    }

    public Seat insert(Connection con,Seat seat) throws SQLException {
        String sql="INSERT INTO seats(event_id,seat_label,status,held_by,hold_expires_at) VALUES(?,?,?,?,?)";

        try(PreparedStatement ps=con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setLong(1,seat.getEventId());
            ps.setString(2,seat.getSeatLabel());
            ps.setString(3,seat.getStatus().name());
            ps.setObject(4,seat.getHeldBy(), Types.BIGINT);
            ps.setTimestamp(5, SQLUtil.toTimeStamp(seat.getHoldExpiresAt()));

            ps.executeUpdate();

            try(ResultSet keys=ps.getGeneratedKeys()){
                if(keys.next()){
                    seat.setId(keys.getLong(1));
                }
            }

        } return seat;
    }

    public Optional<Seat> findById(long id) throws SQLException{
        String sql="SELECT "+COLUMNS+" FROM seats WHERE id=?";
        try(Connection con= DBConnection.getConnection();
         PreparedStatement ps=con.prepareStatement(sql)
        ){
            ps.setLong(1,id);
            try(ResultSet rs= ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapRow(rs));
                }
            }
        } return Optional.empty();
    }

    public List<Seat> findByEvent(long eventId) throws SQLException{
        String sql="SELECT "+ COLUMNS+" FROM seats WHERE event_id=? ORDER BY id";
        List<Seat> seat=new ArrayList<>();
        try(Connection con=DBConnection.getConnection();
        PreparedStatement ps=con.prepareStatement(sql)
        ){
            ps.setLong(1,eventId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                   seat.add(mapRow(rs));
                }
            }
        }return seat;
    }

    public static Seat mapRow(ResultSet rs) throws SQLException{
        Seat seat=new Seat();
        seat.setId(rs.getLong("id"));
        seat.setEventId(rs.getLong("event_id"));
        seat.setSeatLabel(rs.getString("seat_label"));
        seat.setStatus(SeatStatus.valueOf(rs.getString("status")));
        seat.setHeldBy(rs.getObject("held_by",Long.class));
        seat.setHoldExpiresAt(SQLUtil.toLocalDateTime(rs.getTimestamp("hold_expires_at")));
        return seat;
    }

    public boolean tryHold(Connection con,long seatId,long userId,int holdSeconds ) throws SQLException{
        String sql="UPDATE seats "+
                  "SET status='HOLD', held_by=?,hold_expires_at=DATE_ADD(NOW(),INTERVAL ? SECOND) "+
                   "WHERE id=? " +
                  "AND (status='AVAILABLE' OR (status = 'HOLD' AND hold_expires_at < NOW()))";

        try(PreparedStatement ps=con.prepareStatement(sql)){
            ps.setLong(1,userId);
            ps.setInt(2,holdSeconds);
            ps.setLong(3,seatId);

          return  ps.executeUpdate()==1;

        }
    }
    public boolean confirmHold(Connection con,long seatId, long userId) throws SQLException{
       String sql="UPDATE seats "+
               "SET status='BOOKED',held_by=NULL,hold_expires_at=NULL "+
               "WHERE id=?   "+
               "AND status='HOLD' AND held_by = ? AND hold_expires_at > NOW()";

       try(PreparedStatement ps=con.prepareStatement(sql)){
           ps.setLong(1,seatId);
           ps.setLong(2,userId);

           return ps.executeUpdate()==1;
       }

    }

    public int releaseExpiredHolds(Connection con) throws SQLException {
        String sql = "UPDATE seats "
                + "SET status = 'AVAILABLE', held_by = NULL, hold_expires_at = NULL "
                + "WHERE status = 'HOLD' AND hold_expires_at < NOW()";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

}

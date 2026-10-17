package com.ticketforge.dao;

import com.ticketforge.model.Event;
import com.ticketforge.util.DBConnection;
import com.ticketforge.util.SQLUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAO {
    public Event insert(Event event)throws SQLException {
        String sql="INSERT INTO events(title,venue,starts_at) VALUES(?,?,?)";
        try(Connection con= DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,event.getTitle());
            ps.setString(2,event.getVenue());
            ps.setTimestamp(3, SQLUtil.toTimeStamp(event.getStartsAt()));
            ps.executeUpdate();
            try(ResultSet keys=ps.getGeneratedKeys()){
                if(keys.next()){
                    event.setId(keys.getLong(1));
                }
            }
        }
        return event;
    }

    public Optional<Event> findById(long id) throws SQLException{
        String sql="SELECT * FROM events WHERE id=?";
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)
        ){
            ps.setLong(1,id);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapRow(rs));
                }
            }
        }return Optional.empty();
    }


    public List<Event> findAll() throws SQLException{
        String sql="SELECT * FROM users";
        List<Event> events=new ArrayList<>();
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
            ResultSet rs=ps.executeQuery()
        ){
           while(rs.next()){
               events.add(mapRow(rs));
           }
        } return events;
    }

    public static Event mapRow(ResultSet rs) throws SQLException{
       Event event=new Event();
                event.setId(rs.getLong("id"));
                event.setTitle(rs.getString("title"));
                event.setVenue(rs.getString("venue"));
                event.setStartsAt(SQLUtil.toLocalDateTime(rs.getTimestamp("starts_at")));
                return event;
    }
}

package com.ticketforge.dao;

import com.ticketforge.model.Role;
import com.ticketforge.model.User;
import com.ticketforge.util.DBConnection;
import com.ticketforge.util.SQLUtil;

import java.sql.*;
import java.util.Optional;

public class UserDAO {
    public static final String COLUMNS="id, name, email, password_hash, role, created_at";

    public User insert(User user) throws SQLException {
        String sql="INSERT INTO users(name,email,password_hash,role) VALUES(?,?,?,?)";
        try(Connection con= DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ){
            ps.setString(1,user.getName());
            ps.setString(2,user.getEmail());
            ps.setString(3,user.getPasswordHash());
            ps.setString(4,user.getRole().name());
            ps.executeUpdate();
            try(ResultSet keys= ps.getGeneratedKeys())
            {
                if(keys.next()){
                    user.setId(keys.getLong(1));
                }
            }
        } return user;
    }

    public Optional<User> findById(long id) throws SQLException{
        return findOne("SELECT "+COLUMNS+" FROM users WHERE id=?",id);
    }

    public Optional<User> findByEmail(String email) throws SQLException{
        return findOne("SELECT "+COLUMNS+" FROM users WHERE email=?",email);
    }

    public Optional<User> findOne(String sql,Object param) throws SQLException{
        try(Connection con=DBConnection.getConnection();
           PreparedStatement ps=con.prepareStatement(sql)
        ){
            ps.setObject(1,param);

            try(ResultSet rs= ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapRow(rs));
                }
            }
        }
           return Optional.empty();
    }

    public static User mapRow(ResultSet rs) throws SQLException{
       User user=new User();
       user.setId(rs.getLong("id"));
       user.setName(rs.getString("name"));
       user.setEmail(rs.getString("email"));
       user.setPasswordHash(rs.getString("password_hash"));
       user.setRole(Role.valueOf(rs.getString("role")));
       user.setCreated_at(SQLUtil.toLocalDateTime(rs.getTimestamp("created_at")));
       return user;
    }
}

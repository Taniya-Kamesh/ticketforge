package com.ticketforge.util;

import java.sql.*;

public class DbTestHelper {

    public static void clean() throws SQLException{
        try(Connection con=DBConnection.getConnection()){
            requireTestDatabase(con);
            try(Statement st=con.createStatement()){
                st.executeUpdate("DELETE FROM bookings");
                st.executeUpdate("DELETE FROM seats");
                st.executeUpdate("DELETE FROM events");
                st.executeUpdate("DELETE FROM users");
            }
        }
    }

    public static void expireHold(long seatId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            requireTestDatabase(con);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE seats SET hold_expires_at = DATE_SUB(NOW(), INTERVAL 1 MINUTE) WHERE id = ?")) {
                ps.setLong(1, seatId);
                ps.executeUpdate();
            }
        }
    }

    private static void requireTestDatabase(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
            rs.next();
            String name = rs.getString(1);
            if (name == null || !name.endsWith("_test")) {
                throw new IllegalStateException(
                        "Refusing to run: connected to '" + name + "', which is not a *_test database");
            }
        }
    }

}

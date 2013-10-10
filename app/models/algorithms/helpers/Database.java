package models.algorithms.helpers;

import java.sql.*;

public class Database {

    public static Connection getConnection(String db_driver, String path_to_db) {
        try {
            Class.forName(db_driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection(path_to_db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    public static ResultSet getResultSet(Connection con, String query) {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet results = null;
        try {
            results = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}

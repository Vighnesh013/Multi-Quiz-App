package com.aurionpro.multiQuiz;

import java.sql.*;

public class DatabaseConectivity {

    private static final String URL = "jdbc:mysql://localhost:3306/applicationquiz";
    private static final String USER = "root";
    private static final String PASSWORD = "vighnesh";

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

   
}

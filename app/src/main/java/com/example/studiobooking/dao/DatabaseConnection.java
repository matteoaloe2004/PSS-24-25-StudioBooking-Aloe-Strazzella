package com.example.studiobooking.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bookingdb";
    private static final String USER = "root"; // Cambia se necessario
    private static final String PASSWORD = ""; // Cambia se necessario

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

package ru.aston.repository;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionBuilder {
    Connection getConnection() throws SQLException, ClassNotFoundException;
}

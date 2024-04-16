package ru.aston.repository.impl;

import ru.aston.exception.HttpException;
import ru.aston.repository.ConnectionBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BaseDao {
    private ConnectionBuilder connectionBuilder;

    protected Connection getConnection() throws SQLException {
        try {
            return connectionBuilder.getConnection();
        } catch (ClassNotFoundException e) {
            throw new HttpException(e.getMessage());
        }
    }

    public void setConnectionBuilder(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    public void deleteById(String query, Long id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, id);
            preparedStatement.execute();
        }
    }
}

package ru.aston.util;

import ru.aston.exception.ConflictException;
import ru.aston.model.Event;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class Validator {

    private Validator() {
    }

    public static void checkEventInitiator(Event event, Long userId) {
        if (!userId.equals(event.getInitiatorId())) {
            throw new ConflictException(String.format("User with id %d does not have rights " +
                    "to change an event with id %d", userId, event.getId()));
        }
    }

    public static boolean checkColumn(ResultSet resultSet, String columnName, int columnNumber) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        if (metaData.getColumnCount() <= columnNumber) return false;

        return columnName.trim().equals(metaData.getColumnName(columnNumber));
    }
}

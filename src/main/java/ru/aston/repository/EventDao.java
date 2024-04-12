package ru.aston.repository;

import ru.aston.model.Event;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EventDao {

    void update(Event event) throws SQLException;

    Optional<Event> save(Event event) throws SQLException;

    Optional<Event> findById(Long id) throws SQLException;

    List<Event> findAll() throws SQLException;

    void deleteById(Long id) throws SQLException;

    boolean addParticipants(Long eventId, Long userId) throws SQLException;
}

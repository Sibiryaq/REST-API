package ru.aston.repository;

import ru.aston.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao {

    void update(User user) throws SQLException;

    Optional<User> save(User user) throws SQLException;

    Optional<User> findById(Long id) throws SQLException;

    List<User> findAll() throws SQLException;

    void deleteById(Long id) throws SQLException;
}

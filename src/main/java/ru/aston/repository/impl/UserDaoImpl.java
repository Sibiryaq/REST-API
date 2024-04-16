package ru.aston.repository.impl;

import ru.aston.model.User;
import ru.aston.repository.UserDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl extends BaseDao implements UserDao {

    private static final String INSERT_USER_SQL = "INSERT INTO users " +
            "(name, email) VALUES (?, ?);";
    private static final String UPDATE_USER_SQL = "UPDATE users SET name = ?, email = ? WHERE id = ?";
    private static final String SELECT_ALL_USERS_SQL = "SELECT * FROM users";
    private static final String SELECT_USER_SQL = "SELECT * FROM users WHERE id = ?";
    private static final String DELETE_USER_SQL = "DELETE FROM users WHERE id = ?";

    private static final UserDaoImpl INSTANCE = new UserDaoImpl();

    private UserDaoImpl() {
    }

    public static synchronized UserDaoImpl getInstance() {
        INSTANCE.setConnectionBuilder(new ConnectionBuilderImpl());
        return INSTANCE;
    }

    public void update(User user) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(UPDATE_USER_SQL)) {
                preparedStatement.setString(1, user.getName());
                preparedStatement.setString(2, user.getEmail());
                preparedStatement.setLong(3, user.getId());
                preparedStatement.executeUpdate();
            }
        }
    }

    public Optional<User> save(User user) throws SQLException {
        if (user.getId() == null) {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, user.getName());
                preparedStatement.setString(2, user.getEmail());

                preparedStatement.executeUpdate();
                ResultSet keys = preparedStatement.getGeneratedKeys();
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
                keys.close();
            }
        }
        return Optional.of(user);
    }

    public Optional<User> findById(Long id) throws SQLException {
        User user;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_SQL)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            if (resultSet.getRow() == 0) {
                return Optional.empty();
            }
            user = makeUser(resultSet);
            resultSet.close();

        }
        return Optional.of(user);
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(makeUser(resultSet));
            }
            resultSet.close();
        }
        return users;
    }

    public void deleteById(Long id) throws SQLException {
        super.deleteById(DELETE_USER_SQL, id);
    }

    private User makeUser(ResultSet resultSet) throws SQLException {
        long id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");

        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);

        return user;
    }
}

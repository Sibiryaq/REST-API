package ru.aston.repository.impl;

import ru.aston.model.Contact;
import ru.aston.repository.ContactDao;

import java.sql.*;
import java.util.Optional;

public class ContactDaoImpl extends BaseDao implements ContactDao {

    private static final String INSERT_CONTACT_SQL = "INSERT INTO contact " +
            "(phone, address, event_id) VALUES (?, ?, ?);";
    private static final String SELECT_CONTACT_SQL = "SELECT * FROM contact WHERE id = ?";
    private static final String UPDATE_EVENT_CONTACT_SQL = "UPDATE events SET contact = ? WHERE id = ?";
    private static final String UPDATE_CONTACT_SQL = "UPDATE contact SET phone = ?, address = ? WHERE id = ?";
    private static final ContactDaoImpl INSTANCE = new ContactDaoImpl();

    private ContactDaoImpl() {
    }

    public static synchronized ContactDaoImpl getInstance() {
        INSTANCE.setConnectionBuilder(new ConnectionBuilderImpl());
        return INSTANCE;
    }

    @Override
    public void update(Contact contact) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(UPDATE_CONTACT_SQL)) {
                preparedStatement.setString(1, contact.getPhone());
                preparedStatement.setString(2, contact.getAddress());
                preparedStatement.setLong(3, contact.getId());
                preparedStatement.executeUpdate();
            }
        }
    }

    @Override
    public Optional<Contact> save(Contact contact, Long eventId) throws SQLException {
        try (Connection connection = getConnection()) {
            if (contact.getId() == null) {
                try (PreparedStatement preparedStatement =
                             connection.prepareStatement(INSERT_CONTACT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, contact.getPhone());
                    preparedStatement.setString(2, contact.getAddress());
                    preparedStatement.setLong(3, eventId);

                    preparedStatement.executeUpdate();
                    ResultSet keys = preparedStatement.getGeneratedKeys();
                    if (keys.next()) {
                        contact.setId(keys.getLong(1));
                    }
                    keys.close();
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EVENT_CONTACT_SQL)) {
                    preparedStatement.setLong(1, contact.getId());
                    preparedStatement.setLong(2, eventId);
                    preparedStatement.executeUpdate();
                }
            }
        }
        return Optional.of(contact);
    }

    @Override
    public Optional<Contact> findById(Long id) throws SQLException {
        Contact contact;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CONTACT_SQL)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            if (resultSet.getRow() == 0) {
                return Optional.empty();
            }
            contact = makeContact(resultSet);
            resultSet.close();

        }
        return Optional.of(contact);
    }

    @Override
    public void deleteById(Long id) throws SQLException {
        super.deleteById(EventDaoImpl.DELETE_CONTACT_SQL, id);
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_EVENT_CONTACT_SQL)) {
            statement.setNull(1, Types.BIGINT);
            statement.setLong(2, id);
            statement.executeUpdate();
        }

    }

    private Contact makeContact(ResultSet resultSet) throws SQLException {
        long id = resultSet.getInt("id");
        String phone = resultSet.getString("phone");
        String address = resultSet.getString("address");

        Contact contact = new Contact();
        contact.setId(id);
        contact.setPhone(phone);
        contact.setAddress(address);

        return contact;
    }
}

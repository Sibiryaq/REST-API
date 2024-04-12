package ru.aston.repository.impl;

import ru.aston.model.Contact;
import ru.aston.model.Event;
import ru.aston.model.User;
import ru.aston.repository.EventDao;
import ru.aston.util.Validator;

import java.sql.*;
import java.util.*;

public class EventDaoImpl extends BaseDao implements EventDao {

    private static final String INSERT_EVENT_SQL = "INSERT INTO events " +
            "(title, description, initiator) VALUES (?, ?, ?);";

    private static final String SELECT_ALL_EVENTS_SQL = "SELECT e.id, title, description, init.id as init_id," +
            " init.email as init_email, init.name as init_name FROM events AS e LEFT JOIN users AS init ON e.initiator = init.id";
    private static final String SELECT_EVENT_SQL = "SELECT event.id, title, description, " +
            "init.id as init_id, init.email as init_email, init.name as init_name, " +
            "partic.id as partic_id, partic.name as partic_name, partic.email as partic_email, cont.id as cont_id, cont.phone, cont.address " +
            " FROM events AS event  LEFT JOIN participants AS p ON event.id = p.event_id  " +
            "LEFT JOIN  users AS partic ON p.participant_id = partic.id  " +
            "LEFT JOIN contact AS cont ON event.contact = cont.id " +
            "LEFT  JOIN public.users init on init.id = event.initiator WHERE event.id = ?";
    private static final String SELECT_PARTICIPANT_SQL = "SELECT * FROM participants WHERE event_id = ? AND participant_id = ?";
    private static final String DELETE_EVENT_SQL = "DELETE FROM events WHERE id = ?";
    private static final String DELETE_PARTICIPANT_SQL = "DELETE FROM participants WHERE event_id = ? AND participant_id = ?";
    protected static final String DELETE_CONTACT_SQL = "DELETE FROM contact WHERE id = ?";
    private static final String UPDATE_EVENT_SQL = "UPDATE events SET title = ?, description = ? WHERE id = ?";
    private static final String INSERT_PARTICIPANT_SQL = "INSERT INTO participants (event_id, participant_id) VALUES (?, ?)";


    private static final EventDaoImpl INSTANCE = new EventDaoImpl();

    private EventDaoImpl() {
    }

    public static synchronized EventDaoImpl getInstance() {
        INSTANCE.setConnectionBuilder(new ConnectionBuilderImpl());
        return INSTANCE;
    }

    public void update(Event event) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(UPDATE_EVENT_SQL)) {
                preparedStatement.setString(1, event.getTitle());
                preparedStatement.setString(2, event.getDescription());
                preparedStatement.setLong(3, event.getId());
                preparedStatement.executeUpdate();
            }
        }
    }

    @Override
    public Optional<Event> save(Event event) throws SQLException {
        if (event.getId() == null) {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(INSERT_EVENT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, event.getTitle());
                preparedStatement.setString(2, event.getDescription());
                preparedStatement.setLong(3, event.getInitiatorId());

                preparedStatement.executeUpdate();
                ResultSet keys = preparedStatement.getGeneratedKeys();
                if (keys.next()) {
                    event.setId(keys.getLong(1));
                }
                keys.close();
            }
        }
        return Optional.of(event);
    }

    @Override
    public Optional<Event> findById(Long id) throws SQLException {
        Event event;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_EVENT_SQL)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            if (resultSet.getRow() == 0) {
                return Optional.empty();
            }
            event = makeEvent(resultSet);
            resultSet.close();

        }
        return Optional.of(event);
    }

    @Override
    public List<Event> findAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_EVENTS_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                events.add(makeEvent(resultSet));
            }
            resultSet.close();
        }
        return events;
    }

    @Override
    public void deleteById(Long id) throws SQLException {
        super.deleteById(DELETE_EVENT_SQL, id);
    }

    @Override
    public boolean addParticipants(Long eventId, Long userId) throws SQLException {
        try (Connection connection = getConnection()) {
            if (!isParticipant(eventId, userId, connection)) {
                try (PreparedStatement statement = connection.prepareStatement(INSERT_PARTICIPANT_SQL)) {
                    statement.setLong(1, eventId);
                    statement.setLong(2, userId);
                    statement.executeUpdate();
                    return true;
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(DELETE_PARTICIPANT_SQL)) {
                    statement.setLong(1, eventId);
                    statement.setLong(2, userId);
                    statement.execute();
                    return false;
                }
            }
        }
    }

    private boolean isParticipant(Long eventId, Long userId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PARTICIPANT_SQL)) {
            statement.setLong(1, eventId);
            statement.setLong(2, userId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            if (resultSet.getRow() == 0) {
                resultSet.close();
                return false;
            }
            resultSet.close();
            return true;
        }
    }

    private Event makeEvent(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        User initiator = makeInitiator(resultSet);
        Contact contact = makeContact(resultSet);
        Set<User> participants = makeParticipants(resultSet);

        Event event = new Event();
        event.setId(id);
        event.setTitle(title);
        event.setDescription(description);
        event.setInitiator(initiator);
        event.setParticipants(participants);
        event.setContact(contact);

        return event;
    }

    private Contact makeContact(ResultSet resultSet) throws SQLException {
        Contact contact = new Contact();
        if (Validator.checkColumn(resultSet, "cont_id", 10)) {
            contact.setId(resultSet.getLong("cont_id"));
            contact.setPhone(resultSet.getString("phone"));
            contact.setAddress(resultSet.getString("address"));
            return contact;
        }
        return null;
    }

    private Set<User> makeParticipants(ResultSet resultSet) throws SQLException {
        Set<User> participants = new HashSet<>();

        User user = makeParticipant(resultSet);
        if (user == null || user.getId() == 0) return participants;
        participants.add(user);
        while (resultSet.next()) {
            participants.add(makeParticipant(resultSet));
        }
        return participants;
    }

    private User makeParticipant(ResultSet resultSet) throws SQLException {
        if (Validator.checkColumn(resultSet, "partic_id", 7)) {
            User user = new User();
            user.setId(resultSet.getLong("partic_id"));
            user.setName(resultSet.getString("partic_name"));
            user.setEmail(resultSet.getString("partic_email"));
            return user;
        }
        return null;
    }

    private User makeInitiator(ResultSet resultSet) throws SQLException {
        User user = null;

        if (Validator.checkColumn(resultSet, "init_id", 4)) {
            user = new User();
            user.setId(resultSet.getLong("init_id"));
            user.setName(resultSet.getString("init_name"));
            user.setEmail(resultSet.getString("init_email"));
        }
        return user;
    }
}

package ru.aston.util;

import ru.aston.exception.ConflictException;
import ru.aston.exception.EntityNotFoundException;
import ru.aston.exception.HttpException;
import ru.aston.model.Contact;
import ru.aston.model.Event;
import ru.aston.model.User;
import ru.aston.repository.ContactDao;
import ru.aston.repository.EventDao;
import ru.aston.repository.UserDao;
import ru.aston.repository.impl.ContactDaoImpl;
import ru.aston.repository.impl.EventDaoImpl;
import ru.aston.repository.impl.UserDaoImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class GetProvider {

    private GetProvider() {
    }

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    public static UserDao getUserDao() {
        return UserDaoImpl.getInstance();
    }

    public static EventDao getEventDao() {
        return EventDaoImpl.getInstance();
    }

    public static ContactDao getContactDao() {
        return ContactDaoImpl.getInstance();
    }

    public static User getUser(Long id) throws SQLException {
        return getUserDao().findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("User with id %d not found", id)));
    }

    public static Event getEvent(Long id) throws SQLException {
        return getEventDao().findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Event with id %d not found", id)));
    }

    public static Contact getContact(Long id) throws SQLException {
        return getContactDao().findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Contact with id %d not found", id)));
    }

    public static Long getEntityId(String requestPath) {
        Long id;
        if (requestPath == null) throw new ConflictException("Request path must contain an id");
        try {
            String[] pathArray = requestPath.split("/");
            id = Long.parseLong(pathArray[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ConflictException("incorrect path");
        }
        return id;
    }

    public static String getBody(HttpServletRequest req) {
        try {
            req.setCharacterEncoding(Constants.CHARACTER_ENCODING);
            return req.getReader().lines().collect(Collectors.joining());
        } catch (IOException e) {
            throw new HttpException(e.getMessage());
        }
    }
}

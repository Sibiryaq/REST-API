package ru.aston.service;

import ru.aston.dto.ContactDto;

import java.sql.SQLException;

public interface ContactService {

    ContactDto saveContact(ContactDto contactDto, Long eventId, Long userId) throws SQLException;

    Long deleteContact(Long contactId, Long eventId, Long userId) throws SQLException;
}

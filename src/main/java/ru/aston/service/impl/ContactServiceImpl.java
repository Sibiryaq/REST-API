package ru.aston.service.impl;

import ru.aston.dto.ContactDto;
import ru.aston.mapper.ContactMapper;
import ru.aston.model.Contact;
import ru.aston.model.Event;
import ru.aston.repository.ContactDao;
import ru.aston.service.ContactService;
import ru.aston.util.GetProvider;
import ru.aston.util.Validator;

import java.sql.SQLException;
import java.util.Optional;

public class ContactServiceImpl implements ContactService {

    private final ContactDao contactDao;

    public ContactServiceImpl(ContactDao contactDao) {
        this.contactDao = contactDao;
    }

    @Override
    public ContactDto saveContact(ContactDto contactDto, Long eventId, Long userId) throws SQLException {
        Event event = GetProvider.getEvent(eventId);
        GetProvider.getUser(userId);
        Validator.checkEventInitiator(event, userId);
        Contact savedContact = null;
        if (event.getContactId() == 0) {
            Contact contact = ContactMapper.toEntity(contactDto, eventId);
            Optional<Contact> optional = contactDao.save(contact, eventId);
            if (optional.isPresent()) savedContact = optional.get();
        } else {
            savedContact = GetProvider.getContact(event.getContactId());
            savedContact.setAddress(contactDto.getAddress() == null ? savedContact.getAddress() : contactDto.getAddress());
            savedContact.setPhone(contactDto.getPhone() == null ? savedContact.getPhone() : contactDto.getPhone());
            contactDao.update(savedContact);
        }
        return ContactMapper.toDto(savedContact);
    }

    @Override
    public Long deleteContact(Long contactId, Long eventId, Long userId) throws SQLException {
        GetProvider.getUser(userId);
        Event event = GetProvider.getEvent(eventId);
        Validator.checkEventInitiator(event, userId);
        contactDao.deleteById(contactId);
        return contactId;
    }
}

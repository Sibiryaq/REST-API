package ru.aston.testUtil;

import ru.aston.model.Contact;
import ru.aston.model.Event;
import ru.aston.model.User;

public class TestGetProvider {

    public static User getUser(String username, String email) {
        User user = new User();
        user.setName(username);
        user.setEmail(email);
        return user;
    }

    public static Event getEvent(String title, String description, User initiator) {
        Event secondEvent = new Event();
        secondEvent.setTitle(title);
        secondEvent.setDescription(description);
        secondEvent.setInitiator(initiator);
        return secondEvent;
    }

    public static Contact getContact(String phone, String address, Long eventId) {
        Contact contact = new Contact();
        contact.setPhone(phone);
        contact.setAddress(address);
        contact.setEventId(eventId);

        return contact;
    }
}


package ru.aston.service.impl;

import ru.aston.dto.EventDto;
import ru.aston.dto.EventResponseDto;
import ru.aston.dto.EventShortDto;
import ru.aston.dto.UserDto;
import ru.aston.exception.HttpException;
import ru.aston.mapper.EventMapper;
import ru.aston.mapper.UserMapper;
import ru.aston.model.Event;
import ru.aston.model.User;
import ru.aston.repository.EventDao;
import ru.aston.service.EventService;
import ru.aston.util.GetProvider;
import ru.aston.util.Validator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventServiceImpl implements EventService {

    private final EventDao eventDao;

    public EventServiceImpl(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Override
    public EventResponseDto saveEvent(EventDto eventDto, Long userId) throws SQLException {
        User initiator = GetProvider.getUser(userId);
        Event savedEvent;
        if (eventDto.getId() == null) {
            Event event = EventMapper.toEntity(eventDto, initiator);
            savedEvent = eventDao.save(event).orElseThrow(() ->
                    new HttpException("Event was not saved " + eventDto));
        } else {
            savedEvent = GetProvider.getEvent(eventDto.getId());
            Validator.checkEventInitiator(savedEvent, userId);
            savedEvent.setTitle(eventDto.getTitle() == null ? savedEvent.getTitle() : eventDto.getTitle());
            savedEvent.setDescription(eventDto.getDescription() == null ? savedEvent.getDescription() : eventDto.getDescription());
            eventDao.update(savedEvent);
        }
        return EventMapper.toResponseDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEvents(String requestPath) throws SQLException {
        List<Event> events = new ArrayList<>();
        if (requestPath != null) {
            Long id = GetProvider.getEntityId(requestPath);
            Event eventById = GetProvider.getEvent(id);
            events.add(eventById);
            return events.stream().map(EventMapper::toResponseDto).collect(Collectors.toList());
        } else {
            events = eventDao.findAll();
        }
        return events.stream().map(EventMapper::toShortDto).toList();
    }

    @Override
    public Long deleteEvent(String requestPath, Long userId) throws SQLException {
        Long id = GetProvider.getEntityId(requestPath);
        Event event = GetProvider.getEvent(id);
        Validator.checkEventInitiator(event, userId);
        eventDao.deleteById(id);

        return id;
    }

    @Override
    public EventResponseDto addParticipant(Long eventId, Long userId) throws SQLException {
        Event event = GetProvider.getEvent(eventId);
        User user = GetProvider.getUser(userId);
        boolean isAdd = eventDao.addParticipants(eventId, userId);
        Set<UserDto> participants = event.getParticipants()
                .stream().map(UserMapper::toDto).collect(Collectors.toSet());

        if (isAdd) {
            participants.add(UserMapper.toDto(user));
        } else {
            participants.remove(UserMapper.toDto(user));
        }

        return EventMapper.toResponseDto(event, participants);
    }
}

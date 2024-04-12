package ru.aston.service;

import ru.aston.dto.EventDto;
import ru.aston.dto.EventResponseDto;
import ru.aston.dto.EventShortDto;

import java.sql.SQLException;
import java.util.List;

public interface EventService {

    EventResponseDto saveEvent(EventDto eventDto, Long userId) throws SQLException;

    List<EventShortDto> getEvents(String requestPath) throws SQLException;

    Long deleteEvent(String requestPath, Long userId) throws SQLException;

    EventResponseDto addParticipant(Long eventId, Long userId) throws SQLException;
}

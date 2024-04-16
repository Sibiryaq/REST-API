package ru.aston.mapper;

import ru.aston.dto.EventDto;
import ru.aston.dto.EventResponseDto;
import ru.aston.dto.EventShortDto;
import ru.aston.dto.UserDto;
import ru.aston.model.Event;
import ru.aston.model.User;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EventMapper {

    private EventMapper() {
    }

    public static Event toEntity(EventDto eventDto, User initiator) {
        if (eventDto == null) return null;

        Event event = toEntity(eventDto);

        event.setInitiator(initiator);
        event.setParticipants(new HashSet<>());

        return event;
    }

    public static Event toEntity(EventDto eventDto) {
        if (eventDto == null) return null;

        Event event = new Event();
        event.setTitle(eventDto.getTitle());
        event.setDescription(eventDto.getDescription());

        return event;
    }

    public static EventDto toDto(Event event) {
        if (event == null) return null;

        EventDto eventDto = new EventDto();
        eventDto.setTitle(event.getTitle());
        eventDto.setDescription(event.getDescription());
        eventDto.setId(event.getId());

        return eventDto;
    }


    public static EventResponseDto toResponseDto(Event event, Set<UserDto> participants) {
        EventResponseDto eventResponseDto = toResponseDto(event);
        if (eventResponseDto == null) return null;

        eventResponseDto.setParticipants(participants);
        return eventResponseDto;
    }

    public static EventResponseDto toResponseDto(Event event) {
        if (event == null) return null;

        EventResponseDto eventResponseDto = new EventResponseDto();
        eventResponseDto.setId(event.getId());
        eventResponseDto.setTitle(event.getTitle());
        eventResponseDto.setDescription(event.getDescription());
        eventResponseDto.setInitiator(UserMapper.toDto(event.getInitiator()));
        eventResponseDto.setContacts(event.getContact() == null ? null : ContactMapper.toDto(event.getContact()));
        eventResponseDto.setParticipants(event.getParticipants() == null ?
                new HashSet<>() : event.getParticipants().stream().map(UserMapper::toDto).collect(Collectors.toSet()));

        return eventResponseDto;
    }

    public static EventShortDto toShortDto(Event event) {
        if (event == null) return null;

        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .initiator(UserMapper.toDto(event.getInitiator()))
                .build();
    }
}

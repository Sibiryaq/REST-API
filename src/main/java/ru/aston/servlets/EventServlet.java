package ru.aston.servlets;

import ru.aston.dto.ContactDto;
import ru.aston.dto.EventDto;
import ru.aston.dto.EventResponseDto;
import ru.aston.dto.EventShortDto;
import ru.aston.exception.ConflictException;
import ru.aston.exception.EntityNotFoundException;
import ru.aston.exception.HttpException;
import ru.aston.exception.Response;
import ru.aston.service.ContactService;
import ru.aston.service.EventService;
import ru.aston.service.impl.ContactServiceImpl;
import ru.aston.service.impl.EventServiceImpl;
import ru.aston.util.Constants;
import ru.aston.util.GetProvider;
import ru.aston.util.ResponseSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@WebServlet("/event/*")
public class EventServlet extends HttpServlet {

    private final EventService eventService;
    private final ContactService contactService;

    private final ResponseSender responseSender;

    public EventServlet() {
        this.eventService = new EventServiceImpl(GetProvider.getEventDao());
        this.contactService = new ContactServiceImpl(GetProvider.getContactDao());
        this.responseSender = new ResponseSender();
    }

    public EventServlet(EventService eventService, ContactService contactService, ResponseSender responseSender) {
        this.eventService = eventService;
        this.contactService = contactService;
        this.responseSender = responseSender;
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        EventResponseDto eventResponseDto;
        try {
            Long userId = Long.valueOf(req.getHeader(Constants.X_SHARER_USER_ID));
            String pathInfo = req.getPathInfo();
            if (isParticipantPath(pathInfo)) {
                String[] pathInfoArr = pathInfo.split("/");
                Long eventId = Long.valueOf(pathInfoArr[1]);
                eventResponseDto = eventService.addParticipant(eventId, userId);
                responseSender.sendResponse(resp, HttpServletResponse.SC_OK, eventResponseDto);
            } else {
                throw new HttpException("Incorrect request path");
            }
        } catch (SQLException | EntityNotFoundException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
        } catch (NumberFormatException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new Response(e.getMessage()));
        } catch (HttpException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String requestPath = req.getPathInfo();
        try {
            List<EventShortDto> events = eventService.getEvents(requestPath);
            if (requestPath != null) {
                responseSender.sendResponse(resp, HttpServletResponse.SC_OK, events.get(0));
            } else {
                responseSender.sendResponse(resp, HttpServletResponse.SC_OK, events);
            }
        } catch (SQLException | EntityNotFoundException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
        } catch (HttpException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        EventResponseDto eventResponseDto;
        String body = GetProvider.getBody(req);
        try {
            Long userId = Long.valueOf(req.getHeader(Constants.X_SHARER_USER_ID));
            String pathInfo = req.getPathInfo();
            if (isContactPath(pathInfo)) {
                String[] pathInfoArr = pathInfo.split("/");
                Long eventId = Long.valueOf(pathInfoArr[1]);
                ContactDto contactDto = contactDtoFromJson(body, resp);
                contactDto = contactService.saveContact(contactDto, eventId, userId);
                responseSender.sendResponse(resp, HttpServletResponse.SC_CREATED, contactDto);

            } else {
                EventDto eventDto = eventDtoFromJson(body, resp);
                eventResponseDto = eventService.saveEvent(eventDto, userId);
                responseSender.sendResponse(resp, HttpServletResponse.SC_CREATED, eventResponseDto);
            }
        } catch (SQLException | EntityNotFoundException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
        } catch (NumberFormatException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new Response("Header x-sharer-user-id must contain userId"));
        } catch (HttpException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String requestPath = req.getPathInfo();
        try {
            Long userId = Long.valueOf(req.getHeader(Constants.X_SHARER_USER_ID));
            if (isContactPath(requestPath)) {
                String[] pathInfoArr = requestPath.split("/");
                Long eventId = Long.valueOf(pathInfoArr[1]);
                Long contactId = Long.valueOf(pathInfoArr[3]);
                contactId = contactService.deleteContact(contactId, eventId, userId);
                responseSender.sendResponse(resp, HttpServletResponse.SC_OK,
                        new Response(String.format("Contact with id %d was successfully deleted", contactId)));
            } else {
                Long id = eventService.deleteEvent(requestPath, userId);
                responseSender.sendResponse(resp, HttpServletResponse.SC_OK,
                        new Response(String.format("Event with id %d was successfully deleted", id)));
            }
        } catch (SQLException | EntityNotFoundException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
        } catch (ConflictException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            responseSender.sendResponse(resp, HttpServletResponse.SC_CONFLICT,
                    new Response(e.getMessage()));
        } catch (NumberFormatException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new Response("Header x-sharer-user-id must contain userId"));
        } catch (HttpException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    private EventDto eventDtoFromJson(String body, HttpServletResponse resp) {
        EventDto eventDto = null;
        try {
            eventDto = GetProvider.getObjectMapper().readValue(body, EventDto.class);
        } catch (JsonProcessingException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new Response("Incorrect Json received"));
        }
        return eventDto;
    }

    private boolean isContactPath(String pathInfo) {
        if (pathInfo == null) return false;

        String[] pathInfoArr = pathInfo.split("/");
        return (3 <= pathInfoArr.length) && "contact".equalsIgnoreCase(pathInfoArr[2]);
    }

    private boolean isParticipantPath(String pathInfo) {
        if (pathInfo == null) return false;

        String[] pathInfoArr = pathInfo.split("/");
        return (3 == pathInfoArr.length) && "participants".equalsIgnoreCase(pathInfoArr[2]);
    }

    private ContactDto contactDtoFromJson(String body, HttpServletResponse resp) {
        ContactDto contactDto = null;
        try {
            contactDto = GetProvider.getObjectMapper().readValue(body, ContactDto.class);
        } catch (JsonProcessingException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new Response("Incorrect Json received"));
        }
        return contactDto;
    }
}

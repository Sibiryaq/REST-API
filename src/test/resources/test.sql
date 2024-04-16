DROP TABLE IF EXISTS CONTACT, USERS, EVENTS, PARTICIPANTS;

CREATE TABLE IF NOT EXISTS USERS(
                                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    name VARCHAR NOT NULL,
                                    email VARCHAR UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS EVENTS(
                                     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     title VARCHAR NOT NULL,
                                     description TEXT NOT NULL,
                                     contact BIGINT UNIQUE,
                                     initiator BIGINT NOT NULL,

                                     CONSTRAINT EVENTS_INITIATOR_ID_FK FOREIGN KEY (initiator) REFERENCES USERS(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS CONTACT(
                                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      phone VARCHAR,
                                      address VARCHAR,
                                      event_id BIGINT NOT NULL,
    CONSTRAINT CONTACT_EVENT_ID_FK FOREIGN KEY (event_id) REFERENCES EVENTS(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PARTICIPANTS(
                                        event_id BIGINT NOT NULL,
                                        participant_id BIGINT NOT NULL,

    CONSTRAINT PARTICIPANTS_EVENT_ID_FK FOREIGN KEY (event_id) REFERENCES EVENTS(id) ON DELETE CASCADE,
    CONSTRAINT PARTICIPANTS_USER_ID_FK FOREIGN KEY (participant_id) REFERENCES USERS(id)
)

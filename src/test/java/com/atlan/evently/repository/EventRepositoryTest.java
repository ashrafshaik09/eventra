package com.atlan.evently.repository;

import com.atlan.evently.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@DataJpaTest
class EventRepositoryTest {
    
    @Autowired
    private EventRepository eventRepository;

    private ZonedDateTime now;

    @BeforeEach
    void setUp() {
        now = ZonedDateTime.now();
        Event event1 = Event.builder()
                .id("1")
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(now.plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(now)
                .version(1)
                .build();
        Event event2 = Event.builder()
                .id("2")
                .name("Workshop 2025")
                .venue("Community Center")
                .startsAt(now.minusDays(1))
                .capacity(50)
                .availableSeats(50)
                .createdAt(now)
                .version(1)
                .build();
        eventRepository.save(event1);
        eventRepository.save(event2);
    }

    @Test
    void testFindAllByStartsAtAfterReturnsFutureEvents() {
        Page<Event> page = eventRepository.findAllByStartsAtAfter(now, PageRequest.of(0, 10));
        assertEquals(1, page.getContent().size());
        assertEquals("Concert 2025", page.getContent().get(0).getName());
    }

    @Test
    void testFindByIdAndStartsAtAfterReturnsFutureEvent() {
        Event event = eventRepository.findByIdAndStartsAtAfter("1", now);
        assertNotNull(event);
        assertEquals("Concert 2025", event.getName());
    }
}
package com.atlan.evently.service;

import com.atlan.evently.model.Event;
import com.atlan.evently.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public Page<Event> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findAllByStartsAtAfter(ZonedDateTime.now(), pageable);
    }
}
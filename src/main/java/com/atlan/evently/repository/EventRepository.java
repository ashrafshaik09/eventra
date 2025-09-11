package com.atlan.evently.repository;

import com.atlan.evently.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findAllByStartsAtAfter(ZonedDateTime startsAt, Pageable pageable);

    Event findByIdAndStartsAtAfter(UUID id, ZonedDateTime startsAt);
}
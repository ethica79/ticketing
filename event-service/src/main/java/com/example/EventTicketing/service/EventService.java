package com.example.EventTicketing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.EventTicketing.entity.Event;

@Service
public class EventService {

    // In-memory storage for now; replaced by a database in step 6.
    private final Map<Long, Event> events = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public EventService() {
        create(new Event("Daft Punk Tribute", "Olympia, Paris",
                LocalDateTime.of(2027, 3, 12, 20, 0), new BigDecimal("45.00"), 200));
        create(new Event("Jazz Night", "New Morning, Paris",
                LocalDateTime.of(2027, 4, 2, 21, 30), new BigDecimal("25.00"), 80));
        create(new Event("Tech Conference", "Station F, Paris",
                LocalDateTime.of(2027, 5, 20, 9, 0), new BigDecimal("0.00"), 500));
    }

    public List<Event> getEvents() {
        return new ArrayList<>(events.values());
    }

    public Event getEvent(Long id) {
        Event event = events.get(id);
        if (event == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        return event;
    }

    public Event create(Event event) {
        event.setId(nextId.getAndIncrement());
        // A new event always starts with every seat available, whatever the client sent.
        event.setAvailableSeats(event.getTotalSeats());
        events.put(event.getId(), event);
        return event;
    }

    public Event update(Long id, Event updated) {
        Event event = getEvent(id);
        // Seats already booked must stay booked: the new capacity can't go below them.
        int bookedSeats = event.getTotalSeats() - event.getAvailableSeats();
        if (updated.getTotalSeats() < bookedSeats) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "totalSeats can't be lower than the " + bookedSeats + " seats already booked");
        }
        event.setName(updated.getName());
        event.setVenue(updated.getVenue());
        event.setDate(updated.getDate());
        event.setPrice(updated.getPrice());
        event.setTotalSeats(updated.getTotalSeats());
        event.setAvailableSeats(updated.getTotalSeats() - bookedSeats);
        return event;
    }

    public void delete(Long id) {
        if (events.remove(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
    }
}

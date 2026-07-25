package in.guvi.event.management.system.service;

import in.guvi.event.management.system.dto.EventDto;
import in.guvi.event.management.system.dto.EventSearchDto;
import in.guvi.event.management.system.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {

    Event createEvent(EventDto dto, Long adminUserId);

    Event updateEvent(Long id, EventDto dto);

    void deleteEvent(Long id);

    Event findById(Long id);

    Page<Event> searchEvents(EventSearchDto searchDto, Pageable pageable);

    List<Event> getUpcomingEvents();

    EventDto toDto(Event event);

    long countUpcomingEvents();

    long countTotalEvents();
}

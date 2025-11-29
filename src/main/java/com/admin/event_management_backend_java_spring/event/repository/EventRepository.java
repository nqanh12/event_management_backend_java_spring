package com.admin.event_management_backend_java_spring.event.repository;

import com.admin.event_management_backend_java_spring.event.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Date;
import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    
    // Pagination methods
    Page<Event> findAll(Pageable pageable);
    Page<Event> findByStatus(Event.EventStatus status, Pageable pageable);
    Page<Event> findByType(Event.EventType type, Pageable pageable);
    Page<Event> findByDepartmentId(String departmentId, Pageable pageable);
    Page<Event> findByOrganizerId(String organizerId, Pageable pageable);
    
    // Date range queries with pagination
    Page<Event> findByStartTimeBetween(Date startDate, Date endDate, Pageable pageable);
    Page<Event> findByStartTimeGreaterThanEqual(Date startDate, Pageable pageable);
    Page<Event> findByEndTimeLessThanEqual(Date endDate, Pageable pageable);
    
    // Combined filters with pagination
    Page<Event> findByStatusAndType(Event.EventStatus status, Event.EventType type, Pageable pageable);
    Page<Event> findByDepartmentIdAndStatus(String departmentId, Event.EventStatus status, Pageable pageable);
    
    // Search events by name
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    Page<Event> searchEventsByName(String name, Pageable pageable);
    
    // Upcoming events
    @Query("{'startTime': {$gte: ?0}, 'status': {$in: ['APPROVED', 'ONGOING']}}")
    Page<Event> findUpcomingEvents(Date currentDate, Pageable pageable);
    
    // Events by organizer with status
    Page<Event> findByOrganizerIdAndStatus(String organizerId, Event.EventStatus status, Pageable pageable);
    
    // Count methods for performance
    long countByStatus(Event.EventStatus status);
    long countByType(Event.EventType type);
    long countByDepartmentId(String departmentId);
    long countByOrganizerId(String organizerId);
    
    // Count by date range for dashboard filtering
    @Query(value = "{'startTime': {$gte: ?0, $lte: ?1}}", count = true)
    long countByStartTimeBetween(Date startDate, Date endDate);
    
    // Count by department and date range
    @Query(value = "{'$or': [{'department.$id': ?0}, {'department': ?0}], 'startTime': {$gte: ?1, $lte: ?2}}", count = true)
    long countByDepartmentIdAndStartTimeBetween(String departmentId, Date startDate, Date endDate);
    
    // Top events by participants (for dashboard)
    @Query(value = "{}", sort = "{'maxParticipants': -1}")
    List<Event> findTopEventsByParticipants(Pageable pageable);
    
    // Recent events
    @Query(value = "{}", sort = "{'startTime': -1}")
    Page<Event> findRecentEvents(Pageable pageable);

    Page<Event> findByIsDeletedFalse(Pageable pageable);
    List<Event> findByIsDeletedFalse();
    Page<Event> findByStatusAndIsDeletedFalse(Event.EventStatus status, Pageable pageable);
    Page<Event> findByTypeAndIsDeletedFalse(Event.EventType type, Pageable pageable);
    Page<Event> findByDepartmentIdAndIsDeletedFalse(String departmentId, Pageable pageable);
    Page<Event> findByOrganizerIdAndIsDeletedFalse(String organizerId, Pageable pageable);
    Page<Event> findByStartTimeBetweenAndIsDeletedFalse(Date startDate, Date endDate, Pageable pageable);
    Page<Event> findByStatusAndTypeAndIsDeletedFalse(Event.EventStatus status, Event.EventType type, Pageable pageable);
    Page<Event> findByDepartmentIdAndStatusAndIsDeletedFalse(String departmentId, Event.EventStatus status, Pageable pageable);
} 
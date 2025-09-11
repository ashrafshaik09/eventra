package com.atlan.evently.service;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.exception.BookingConflictException;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import com.atlan.evently.repository.EventRepository;
import com.atlan.evently.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConcurrencyStressTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AsyncBookingService asyncBookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User testUser;
    private Event testEvent;
    private final int THREAD_COUNT = 50;
    private final int EVENT_CAPACITY = 10;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .email("concurrency@test.com")
                .name("Concurrency Test User")
                .passwordHash("hashedPassword123")
                .role(User.UserRole.USER)
                .isActive(true)
                .createdAt(ZonedDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Create test event with limited capacity
        testEvent = Event.builder()
                .name("Concurrency Test Event")
                .venue("Test Venue")
                .startsAt(ZonedDateTime.now().plusDays(1))
                .capacity(EVENT_CAPACITY)
                .availableSeats(EVENT_CAPACITY)
                .createdAt(ZonedDateTime.now())
                .version(1)
                .build();
        testEvent = eventRepository.save(testEvent);
    }

    /**
     * Test concurrent booking requests to ensure no overselling
     * Similar to the Medium article's parallel execution testing
     */
    @Test
    void testConcurrentBookingRequests_NoOverselling() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        
        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger failedBookings = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        // Create multiple concurrent booking requests
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    BookingRequest request = new BookingRequest();
                    request.setUserId(testUser.getId().toString());
                    request.setEventId(testEvent.getId().toString());
                    request.setQuantity(1);
                    request.setIdempotencyKey("concurrency-test-" + threadId + "-" + System.nanoTime());

                    BookingResponse response = bookingService.createBooking(request);
                    successfulBookings.incrementAndGet();
                    
                } catch (BookingConflictException e) {
                    failedBookings.incrementAndGet();
                    // Expected when event is sold out
                } catch (Exception e) {
                    exceptions.add(e);
                    failedBookings.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");
        executor.shutdown();

        // Verify results
        System.out.printf("Successful bookings: %d, Failed bookings: %d, Exceptions: %d%n", 
                         successfulBookings.get(), failedBookings.get(), exceptions.size());

        // Critical assertion: no overselling
        assertEquals(EVENT_CAPACITY, successfulBookings.get(), 
                    "Should have exactly " + EVENT_CAPACITY + " successful bookings (no overselling)");
        
        assertEquals(THREAD_COUNT - EVENT_CAPACITY, failedBookings.get(), 
                    "Remaining requests should fail due to sold out event");

        // Verify no unexpected exceptions
        assertTrue(exceptions.isEmpty(), "Should have no unexpected exceptions: " + exceptions);

        // Verify event capacity is correct
        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
        assertEquals(0, updatedEvent.getAvailableSeats(), "Event should be sold out");
    }

    /**
     * Test parallel validation (Medium article pattern)
     */
    @Test
    void testParallelValidationPerformance() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setUserId(testUser.getId().toString());
        request.setEventId(testEvent.getId().toString());
        request.setQuantity(1);
        request.setIdempotencyKey("parallel-validation-test");

        // Measure sequential validation time
        long sequentialStart = System.currentTimeMillis();
        BookingResponse sequentialResult = bookingService.createBooking(request);
        long sequentialTime = System.currentTimeMillis() - sequentialStart;

        // Clean up for parallel test
        bookingService.cancelBooking(sequentialResult.getBookingId());

        // Measure parallel validation time
        long parallelStart = System.currentTimeMillis();
        BookingResponse parallelResult = asyncBookingService.createBookingWithParallelValidation(request);
        long parallelTime = System.currentTimeMillis() - parallelStart;

        System.out.printf("Sequential validation: %dms, Parallel validation: %dms%n", 
                         sequentialTime, parallelTime);

        // Both should succeed
        assertNotNull(sequentialResult);
        assertNotNull(parallelResult);
        
        // Note: Parallel may not always be faster in tests due to overhead, 
        // but it demonstrates the pattern for real-world scenarios
    }

    /**
     * Test bulk parallel processing
     */
    @Test
    void testBulkParallelBookingProcessing() {
        List<BookingRequest> requests = new ArrayList<>();
        
        // Create requests up to event capacity
        for (int i = 0; i < EVENT_CAPACITY; i++) {
            BookingRequest request = new BookingRequest();
            request.setUserId(testUser.getId().toString());
            request.setEventId(testEvent.getId().toString());
            request.setQuantity(1);
            request.setIdempotencyKey("bulk-test-" + i);
            requests.add(request);
        }

        // Process in parallel
        long startTime = System.currentTimeMillis();
        List<BookingResponse> responses = asyncBookingService.processBulkBookings(requests);
        long processingTime = System.currentTimeMillis() - startTime;

        System.out.printf("Processed %d bookings in %dms (parallel)%n", responses.size(), processingTime);

        // Verify all bookings succeeded
        assertEquals(EVENT_CAPACITY, responses.size());
        responses.forEach(response -> {
            assertNotNull(response.getBookingId());
            assertEquals("CONFIRMED", response.getBookingStatus());
        });

        // Verify event is sold out
        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
        assertEquals(0, updatedEvent.getAvailableSeats());
    }
}

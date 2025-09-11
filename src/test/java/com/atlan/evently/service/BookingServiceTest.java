// package com.atlan.evently.service;

// import com.atlan.evently.model.Booking;
// import com.atlan.evently.dto.BookingResponse;
// import com.atlan.evently.repository.BookingRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.util.Collections;
// import java.util.List;
// import java.util.UUID;

// import static org.mockito.Mockito.*;
// import static org.junit.jupiter.api.Assertions.*;

// class BookingServiceTest {

//     @Mock
//     private BookingRepository bookingRepository;

//     @InjectMocks
//     private BookingService bookingService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void testGetUserBookingsWithValidStatus() {
//         UUID userId = UUID.randomUUID();
//         List<Booking> bookings = Collections.singletonList(new Booking());
//         List<Booking> result = bookingService.getUserBookings(userId, "CONFIRMED");

//         List<BookingResponse> result = bookingService.getUserBookings(userId, "CONFIRMED");

//         assertNotNull(result);
//         assertEquals(1, result.size());
//         verify(bookingRepository, times(1)).findByUserIdAndStatus(userId, "CONFIRMED");
//     }
//     @Test
//     void testGetUserBookingsWithoutStatus() {
//         UUID userId = UUID.randomUUID();
//         List<Booking> bookings = Collections.singletonList(new Booking());
//         List<Booking> result = bookingService.getUserBookings(userId, null);

//         List<BookingResponse> result = bookingService.getUserBookings(userId, null);

//         assertNotNull(result);
//         assertEquals(1, result.size());
//         verify(bookingRepository, times(1)).findByUserId(userId);
//     }
//     @Test
//     void testGetUserBookingsWithInvalidStatusThrowsException() {
//         UUID userId = UUID.randomUUID();
//     @Test
//     void testGetUserBookingsWithInvalidUserIdThrowsException() {
//         UUID invalidUserId = UUID.randomUUID();
//         assertThrows(IllegalArgumentException.class, () -> bookingService.getUserBookings(invalidUserId, "PENDING"));
//     }
//         assertThrows(IllegalArgumentException.class, () -> bookingService.getUserBookings("1", "PENDING"));
//     }
// }
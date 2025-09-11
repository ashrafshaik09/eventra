package com.atlan.evently.service;

import com.atlan.evently.dto.events.WaitlistNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Email service for sending waitlist notifications and other email communications.
 * Uses JavaMailSender with MailHog for local testing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${evently.notifications.email.from-address:noreply@evently.com}")
    private String fromAddress;

    @Value("${evently.notifications.email.from-name:Evently Platform}")
    private String fromName;

    @Value("${evently.waitlist.booking-window-minutes:10}")
    private int bookingWindowMinutes;

    /**
     * Send waitlist notification email when a seat becomes available
     */
    public void sendWaitlistNotificationEmail(WaitlistNotificationEvent event) {
        log.info("Sending waitlist notification email to {} for event {}", 
                event.getUserEmail(), event.getEventName());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(String.format("%s <%s>", fromName, fromAddress));
            message.setTo(event.getUserEmail());
            message.setSubject(String.format("🎫 Seat Available: %s", event.getEventName()));
            message.setText(buildWaitlistEmailContent(event));

            mailSender.send(message);
            
            log.info("Successfully sent waitlist notification email to {}", event.getUserEmail());
            
        } catch (Exception e) {
            log.error("Failed to send waitlist notification email to {}: {}", 
                    event.getUserEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmationEmail(String userEmail, String userName, 
                                           String eventName, String eventVenue, 
                                           String eventTime, int quantity, String bookingId) {
        log.info("Sending booking confirmation email to {} for event {}", userEmail, eventName);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(String.format("%s <%s>", fromName, fromAddress));
            message.setTo(userEmail);
            message.setSubject(String.format("✅ Booking Confirmed: %s", eventName));
            message.setText(buildBookingConfirmationContent(userName, eventName, eventVenue, 
                    eventTime, quantity, bookingId));

            mailSender.send(message);
            
            log.info("Successfully sent booking confirmation email to {}", userEmail);
            
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}: {}", 
                    userEmail, e.getMessage(), e);
            // Don't throw here - booking should succeed even if email fails
        }
    }

    /**
     * Send booking cancellation email
     */
    public void sendBookingCancellationEmail(String userEmail, String userName,
                                           String eventName, String eventTime, String bookingId) {
        log.info("Sending booking cancellation email to {} for event {}", userEmail, eventName);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(String.format("%s <%s>", fromName, fromAddress));
            message.setTo(userEmail);
            message.setSubject(String.format("❌ Booking Cancelled: %s", eventName));
            message.setText(buildBookingCancellationContent(userName, eventName, eventTime, bookingId));

            mailSender.send(message);
            
            log.info("Successfully sent booking cancellation email to {}", userEmail);
            
        } catch (Exception e) {
            log.error("Failed to send booking cancellation email to {}: {}", 
                    userEmail, e.getMessage(), e);
            // Don't throw here - cancellation should succeed even if email fails
        }
    }

    // ========== EMAIL CONTENT BUILDERS ==========

    private String buildWaitlistEmailContent(WaitlistNotificationEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm");
        String formattedEventTime = event.getEventStartTime().format(formatter);
        String formattedExpiryTime = event.getExpiresAt().format(DateTimeFormatter.ofPattern("HH:mm"));

        return String.format("""
            Hi %s,
            
            Great news! A seat has become available for the event you're waiting for:
            
            🎪 EVENT DETAILS
            Event: %s
            Venue: %s
            Date & Time: %s
            Available Seats: %d
            
            ⏰ URGENT: Book within %d minutes!
            Your booking window expires at %s today.
            
            👆 BOOK NOW: %s
            
            If you don't book within the time window, we'll offer this seat to the next person on the waitlist.
            
            Need help? Just reply to this email.
            
            Thanks for using Evently!
            
            Best regards,
            The Evently Team
            
            ---
            This is an automated message. Please don't reply directly to this email address.
            """, 
            event.getUserName(),
            event.getEventName(),
            event.getEventVenue(), 
            formattedEventTime,
            event.getAvailableSeats(),
            bookingWindowMinutes,
            formattedExpiryTime,
            event.getBookingUrl()
        );
    }

    private String buildBookingConfirmationContent(String userName, String eventName, 
                                                  String eventVenue, String eventTime, 
                                                  int quantity, String bookingId) {
        return String.format("""
            Hi %s,
            
            Your booking has been confirmed! Here are your ticket details:
            
            🎫 BOOKING CONFIRMATION
            Booking ID: %s
            Event: %s
            Venue: %s
            Date & Time: %s
            Tickets: %d
            
            📱 What's next?
            • Keep this email as your booking confirmation
            • Arrive 30 minutes before the event starts
            • Bring a valid ID for entry
            
            ❓ Need to cancel? 
            You can cancel your booking up to 2 hours before the event starts through our platform.
            
            Thanks for choosing Evently!
            
            Best regards,
            The Evently Team
            """,
            userName, bookingId, eventName, eventVenue, eventTime, quantity
        );
    }

    private String buildBookingCancellationContent(String userName, String eventName, 
                                                  String eventTime, String bookingId) {
        return String.format("""
            Hi %s,
            
            Your booking has been successfully cancelled.
            
            🚫 CANCELLED BOOKING
            Booking ID: %s
            Event: %s
            Date & Time: %s
            
            💰 Refund Information
            If applicable, your refund will be processed within 3-5 business days.
            
            🔄 Changed your mind?
            You can try booking again if seats are still available.
            
            Thanks for using Evently!
            
            Best regards,
            The Evently Team
            """,
            userName, bookingId, eventName, eventTime
        );
    }
}

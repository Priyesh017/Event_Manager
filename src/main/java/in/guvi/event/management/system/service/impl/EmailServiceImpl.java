package in.guvi.event.management.system.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final Resend resendClient;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Value("${resend.from-name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

    @Override
    @Async
    public void sendRegistrationConfirmation(User user, Event event) {
        String subject = "Registration Confirmed: " + event.getTitle();
        String html = buildConfirmationHtml(user, event);
        sendEmail(user.getEmail(), subject, html);
    }

    @Override
    @Async
    public void sendEventReminder(User user, Event event) {
        String subject = "Reminder: " + event.getTitle() + " is Tomorrow!";
        String html = buildReminderHtml(user, event);
        sendEmail(user.getEmail(), subject, html);
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to EventHub!";
        String html = buildWelcomeHtml(user);
        sendEmail(user.getEmail(), subject, html);
    }

    @Override
    @Async
    public void sendEventUpdateNotification(User user, Event event, String changesSummary) {
        String subject = "Event Updated: " + event.getTitle();
        String html = buildEventUpdateHtml(user, event, changesSummary);
        sendEmail(user.getEmail(), subject, html);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private void sendEmail(String to, String subject, String html) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                .from(fromName + " <" + fromEmail + ">")
                .to(to)
                .subject(subject)
                .html(html)
                .build();

            resendClient.emails().send(options);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (ResendException e) {
            log.error("Failed to send email to {} — {}", to, e.getMessage());
            // Do not rethrow — email failure should not break the main flow
        }
    }

    private String buildConfirmationHtml(User user, Event event) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Inter', Arial, sans-serif; background:#0F0F1A; color:#F1F5F9; margin:0; padding:0;">
              <div style="max-width:600px; margin:40px auto; background:#1A1A2E; border-radius:16px; overflow:hidden;">
                <div style="background:linear-gradient(135deg,#6C63FF,#4F46E5); padding:32px; text-align:center;">
                  <h1 style="color:#fff; margin:0; font-size:28px;">🎉 Registration Confirmed!</h1>
                </div>
                <div style="padding:32px;">
                  <p style="font-size:18px;">Hi <strong>%s</strong>,</p>
                  <p>You're officially registered for:</p>
                  <div style="background:#16213E; border-radius:12px; padding:20px; margin:20px 0; border-left:4px solid #6C63FF;">
                    <h2 style="color:#6C63FF; margin:0 0 12px 0;">%s</h2>
                    <p style="margin:4px 0;">📅 <strong>Date:</strong> %s</p>
                    <p style="margin:4px 0;">📍 <strong>Venue:</strong> %s, %s</p>
                    <p style="margin:4px 0;">🏷️ <strong>Category:</strong> %s</p>
                  </div>
                  <p>We look forward to seeing you there! You'll receive a reminder 24 hours before the event.</p>
                  <div style="text-align:center; margin-top:24px;">
                    <a href="%s/events/%d" style="background:linear-gradient(135deg,#6C63FF,#4F46E5); color:#fff; padding:12px 28px; border-radius:8px; text-decoration:none; font-weight:600;">View Event Details</a>
                  </div>
                </div>
                <div style="padding:20px; text-align:center; color:#64748B; font-size:12px; border-top:1px solid rgba(255,255,255,0.05);">
                  © 2026 EventHub. You received this because you registered for an event.
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                user.getName(),
                event.getTitle(),
                event.getEventDate().format(FORMATTER),
                event.getVenue(), event.getLocation(),
                event.getCategory().name(),
                baseUrl, event.getId()
            );
    }

    private String buildReminderHtml(User user, Event event) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Inter', Arial, sans-serif; background:#0F0F1A; color:#F1F5F9; margin:0; padding:0;">
              <div style="max-width:600px; margin:40px auto; background:#1A1A2E; border-radius:16px; overflow:hidden;">
                <div style="background:linear-gradient(135deg,#F59E0B,#D97706); padding:32px; text-align:center;">
                  <h1 style="color:#fff; margin:0; font-size:28px;">⏰ Event Tomorrow!</h1>
                </div>
                <div style="padding:32px;">
                  <p style="font-size:18px;">Hi <strong>%s</strong>,</p>
                  <p>This is a friendly reminder that your event is <strong>tomorrow</strong>!</p>
                  <div style="background:#16213E; border-radius:12px; padding:20px; margin:20px 0; border-left:4px solid #F59E0B;">
                    <h2 style="color:#F59E0B; margin:0 0 12px 0;">%s</h2>
                    <p style="margin:4px 0;">📅 <strong>Date:</strong> %s</p>
                    <p style="margin:4px 0;">📍 <strong>Venue:</strong> %s, %s</p>
                  </div>
                  <p>Don't forget to attend! See you there.</p>
                  <div style="text-align:center; margin-top:24px;">
                    <a href="%s/events/%d" style="background:linear-gradient(135deg,#F59E0B,#D97706); color:#fff; padding:12px 28px; border-radius:8px; text-decoration:none; font-weight:600;">View Event Details</a>
                  </div>
                </div>
                <div style="padding:20px; text-align:center; color:#64748B; font-size:12px; border-top:1px solid rgba(255,255,255,0.05);">
                  © 2026 EventHub.
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                user.getName(),
                event.getTitle(),
                event.getEventDate().format(FORMATTER),
                event.getVenue(), event.getLocation(),
                baseUrl, event.getId()
            );
    }

    private String buildWelcomeHtml(User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Inter', Arial, sans-serif; background:#0F0F1A; color:#F1F5F9; margin:0; padding:0;">
              <div style="max-width:600px; margin:40px auto; background:#1A1A2E; border-radius:16px; overflow:hidden;">
                <div style="background:linear-gradient(135deg,#22C55E,#16A34A); padding:32px; text-align:center;">
                  <h1 style="color:#fff; margin:0; font-size:28px;">Welcome to EventHub! 🚀</h1>
                </div>
                <div style="padding:32px;">
                  <p style="font-size:18px;">Hi <strong>%s</strong>,</p>
                  <p>Your account has been created successfully. You can now browse and register for exciting events!</p>
                  <div style="text-align:center; margin-top:24px;">
                    <a href="%s/events" style="background:linear-gradient(135deg,#22C55E,#16A34A); color:#fff; padding:12px 28px; border-radius:8px; text-decoration:none; font-weight:600;">Browse Events</a>
                  </div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(user.getName(), baseUrl);
    }

    private String buildEventUpdateHtml(User user, Event event, String changesSummary) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Inter', Arial, sans-serif; background:#0F0F1A; color:#F1F5F9; margin:0; padding:0;">
              <div style="max-width:600px; margin:40px auto; background:#1A1A2E; border-radius:16px; overflow:hidden;">
                <div style="background:linear-gradient(135deg,#F59E0B,#D97706); padding:32px; text-align:center;">
                  <h1 style="color:#fff; margin:0; font-size:28px;">📝 Event Updated</h1>
                </div>
                <div style="padding:32px;">
                  <p style="font-size:18px;">Hi <strong>%s</strong>,</p>
                  <p>The event you registered for has been updated. Here's what changed:</p>
                  <div style="background:#16213E; border-radius:12px; padding:20px; margin:20px 0; border-left:4px solid #F59E0B;">
                    <h2 style="color:#F59E0B; margin:0 0 12px 0;">%s</h2>
                    <div style="font-size:0.95rem; line-height:1.7; white-space:pre-line;">%s</div>
                    <hr style="border:none; border-top:1px solid rgba(255,255,255,0.1); margin:16px 0;">
                    <p style="margin:4px 0;">📅 <strong>Starts:</strong> %s</p>
                    <p style="margin:4px 0;">🏁 <strong>Ends:</strong> %s</p>
                    <p style="margin:4px 0;">📍 <strong>Venue:</strong> %s, %s</p>
                  </div>
                  <p>Please plan accordingly. See you there!</p>
                  <div style="text-align:center; margin-top:24px;">
                    <a href="%s/events/%d" style="background:linear-gradient(135deg,#F59E0B,#D97706); color:#fff; padding:12px 28px; border-radius:8px; text-decoration:none; font-weight:600;">View Event</a>
                  </div>
                </div>
                <div style="padding:20px; text-align:center; color:#64748B; font-size:12px; border-top:1px solid rgba(255,255,255,0.05);">
                  © 2026 EventHub.
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                user.getName(),
                event.getTitle(),
                changesSummary,
                event.getEventDate().format(FORMATTER),
                event.getEndDate().format(FORMATTER),
                event.getVenue(), event.getLocation(),
                baseUrl, event.getId()
            );
    }
}

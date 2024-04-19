package ai.geteam.client.service.recruiter;

public interface EmailService {
    void sendInvitationEmail(String recipientEmail,Long userId);
}

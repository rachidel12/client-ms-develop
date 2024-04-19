package ai.geteam.client.service.recruiter;

import ai.geteam.client.feign.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements  EmailService {
    private final EmailSender emailSender;
    public void sendInvitationEmail(String email, Long userId) {
        HashMap<String, Object> dynamicValues = new HashMap<>();
        dynamicValues.put("userId", userId);
        HashMap<String, Object> data = new HashMap<>();
        data.put("subject", "Invitation to Join Our Platform");
        data.put("recipients", List.of(email));
        data.put("template", "invitation");
        data.put("dynamicValues", dynamicValues);
        emailSender.sendEmail(data);
    }

}

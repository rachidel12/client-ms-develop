package ai.geteam.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;

@FeignClient(value = "messaging-service", url = "${ms-messaging-url}")
public interface EmailSender {

    @PostMapping(value = "/send-email", produces = MediaType.APPLICATION_JSON_VALUE)
    void sendEmail(HashMap<String, Object> data);
}

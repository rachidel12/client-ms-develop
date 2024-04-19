package ai.geteam.client.feign;

import org.keycloak.representations.account.UserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "iam-service", url = "${ms-iam-url}")
public interface IamService {
    @GetMapping(value = "/get")
    List<UserRepresentation> getUserByUsername(@RequestParam("realm") String realm, @RequestParam("username") String username, @RequestHeader("Authorization") String authorization);

    @PostMapping(value = "/block")
    ResponseEntity<String> blockRecruiter(@RequestParam("realm") String realm, @RequestParam String email);

    @PostMapping(value = "/unblock")
    ResponseEntity<String> unblockRecruiter(@RequestParam("realm") String realm, @RequestParam String email);
    
    @DeleteMapping(value = "/delete")
    ResponseEntity<String> deleteUser(@RequestParam("realm") String realm, @RequestParam("id") String id, @RequestHeader("Authorization") String authorization);
}

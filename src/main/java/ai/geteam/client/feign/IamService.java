package ai.geteam.client.feign;

import org.keycloak.representations.account.UserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "iam-service", url = "${ms-iam-url}")
public interface IamService {
    @GetMapping(value = "/get")
    List<UserRepresentation> getUserByUsername(@RequestParam("realm") String realm, @RequestParam("username") String username, @RequestHeader("Authorization") String authorization);

    @PostMapping(value = "/block")
    ResponseEntity<String> blockRecruiter(@RequestParam("realm") String realm, @RequestParam String email, @RequestHeader("Authorization") String authorization);

    @PostMapping(value = "/unblock")
    ResponseEntity<String> unblockRecruiter(@RequestParam("realm") String realm, @RequestParam String email, @RequestHeader("Authorization") String authorization);

    @DeleteMapping(value = "/delete")
    ResponseEntity<String> deleteUser(@RequestParam("realm") String realm, @RequestParam("id") String id, @RequestHeader("Authorization") String authorization);
}

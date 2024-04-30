package ai.geteam.client.controller;

import ai.geteam.client.dto.InvitationEmailRequestDTO;
import ai.geteam.client.dto.ClientAccountInfoDTO;
import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.dto.RoleDTO;
import ai.geteam.client.dto.SignatureDTO;
import ai.geteam.client.dto.UserDTO;
import ai.geteam.client.exception.RequestException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.service.recruiter.RecruiterService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
public class RecruiterController {
    private final RecruiterService recruiterService;

    @RateLimiter(name = "getTeamMember")
    @GetMapping("/{userId}/teammate")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<RecruiterDTO> getTeamMember(@NonNull @PathVariable(name = "userId") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(recruiterService.getTeamMember(userId));
    }

    @RateLimiter(name = "getAllTeamMember")
    @GetMapping("/teammates")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<RecruiterDTO>> getAllTeamMember() throws RequestException {
        return ResponseEntity.status(HttpStatus.OK).body(recruiterService.getAllTeamMember());
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<String> sendInvitation(@RequestHeader("Authorization") String authorizationHeader, @RequestBody InvitationEmailRequestDTO invitationRequest) {
        recruiterService.sendInvitation(authorizationHeader, invitationRequest);
        return new ResponseEntity<>("Invitation sent successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<String> deleteClient(
            @RequestHeader(value = "Authorization") String authorization,
            @PathVariable Long clientId) {
        if (recruiterService.deleteClient(clientId, authorization))
            return ResponseEntity.status(204).build();
        return ResponseEntity.status(500).body("Delete Failed");
    }

    @RateLimiter(name = "getClientPersonalInfo")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
	public ResponseEntity<ClientAccountInfoDTO> getClientPersonalInfo() {
	    return ResponseEntity.status(HttpStatus.OK).body(recruiterService.getClientPersonalInfo());
    }

    @PreAuthorize("hasRole('manage-account')")
    @PutMapping("/{userId}/activate-invited-client")
    public ResponseEntity<RecruiterDTO> activateInvitedClient(
            @RequestHeader(name = "Authorization") String authorization,
            @PathVariable Long userId,
            @RequestParam(name = "status") String status) {
        return ResponseEntity.ok().body(recruiterService.activateInvitedClient(userId, status));
    }

    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<RecruiterDTO> activateRecruiter(@NonNull @PathVariable(name = "userId") Long userId, @RequestHeader(name = "Authorization") String authorization) {
        return ResponseEntity.status(HttpStatus.OK).body(recruiterService.updateRecruiterStatus(userId, authorization));
    }
    
    @PutMapping("/{userId}/block")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<RecruiterDTO> blockRecruiter(@PathVariable Long userId, Authentication authentication) throws UnAuthorizedException {
        return ResponseEntity.status(HttpStatus.OK).body(recruiterService.blockRecruiter(userId, authentication));
    }

    @PutMapping("/{userID}/admin")
    @PreAuthorize("hasRole('CLIENT')")
    public RecruiterDTO assignAdminRole(@PathVariable("userID") Long userID,@RequestBody RoleDTO roleDTO){
        return recruiterService.assignAdminRole(userID,roleDTO);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<String> updateRecruiter(@RequestBody UserDTO userDTO) {
        recruiterService.updateRecruiter(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getPhone());
        return ResponseEntity.ok("Vos informations ont été mise à jour avec succés");

    }

    @GetMapping("/signatures/{signatureId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<SignatureDTO> getSignatureById(@PathVariable Long signatureId, @RequestHeader("Authorization") String authorization) {
        SignatureDTO signatureDTO = recruiterService.getSignatureById(signatureId, authorization);
        return ResponseEntity.ok(signatureDTO);
    }

    @PutMapping("/signatures/default/{signatureId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<SignatureDTO> setDefaultSignature(@NonNull @PathVariable(name = "signatureId") Long signatureId, @RequestHeader(name="Authorization") String authorization) {
    	return ResponseEntity.status(HttpStatus.OK).body(recruiterService.setdefaultSignature(signatureId, authorization));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("id")
    public ResponseEntity<String>  getClientId(@RequestParam String email) {
        return ResponseEntity.ok(recruiterService.getClientId(email));
    }
}

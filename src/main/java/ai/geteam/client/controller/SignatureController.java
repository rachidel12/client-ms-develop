package ai.geteam.client.controller;


import ai.geteam.client.exception.RequestException;
import ai.geteam.client.dto.SignatureDTO;
import ai.geteam.client.service.signature.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
public class SignatureController {
    private final SignatureService service;

    @PostMapping("/signatures")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<SignatureDTO> createSignature(@RequestBody SignatureDTO signatureDTO, Authentication authentication){
        return ResponseEntity.status(HttpStatus.OK).body(service.createSignature(signatureDTO,authentication));
    }


    @GetMapping("/signatures")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<SignatureDTO>> getSignatures() throws RequestException {
        return ResponseEntity.status(HttpStatus.OK).body(service.getSignatures());

    }


    @DeleteMapping("/signatures/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<String> delete(@PathVariable Long id,Authentication authentication) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(service.delete(id,authentication));
    }

}

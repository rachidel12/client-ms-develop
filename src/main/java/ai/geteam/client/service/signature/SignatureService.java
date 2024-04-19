package ai.geteam.client.service.signature;

import ai.geteam.client.dto.SignatureDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SignatureService {
    SignatureDTO createSignature(SignatureDTO signatureDTO, Authentication authentication);

    List<SignatureDTO> getSignatures();

    String delete(Long id,Authentication authentication);
}

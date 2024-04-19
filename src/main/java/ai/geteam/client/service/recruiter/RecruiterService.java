package ai.geteam.client.service.recruiter;

import ai.geteam.client.dto.InvitationEmailRequestDTO;
import ai.geteam.client.dto.ClientAccountInfoDTO;
import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.dto.RoleDTO;
import ai.geteam.client.dto.SignatureDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface RecruiterService {
	
    RecruiterDTO getTeamMember(Long userId);

    List<RecruiterDTO> getAllTeamMember();
    
    RecruiterDTO activateInvitedClient(Long userId, String status);

    RecruiterDTO assignAdminRole(Long userID, RoleDTO roleDTO);

    void updateRecruiter(String firstName, String lastName, String phone);

    void sendInvitation(String authorizationHeader, InvitationEmailRequestDTO invitationRequest);

    ClientAccountInfoDTO getClientPersonalInfo();

    RecruiterDTO updateRecruiterStatus(Long userId, String authorization);

    boolean deleteClient(Long clientId, String authorizationToken);

    RecruiterDTO blockRecruiter(Long id, Authentication authentication);

    RecruiterDTO getRecruiterByEmail(String email);

    SignatureDTO setdefaultSignature(Long signatureId, String authorization);

    SignatureDTO getSignatureById(Long signatureId, String authorization);

}

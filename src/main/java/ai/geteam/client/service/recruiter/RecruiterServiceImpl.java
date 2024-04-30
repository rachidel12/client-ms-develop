package ai.geteam.client.service.recruiter;


import ai.geteam.client.dto.*;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.entity.recruiter.Status;
import ai.geteam.client.entity.signatue.Signature;
import ai.geteam.client.exception.BaseException;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.ServerException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.exception.utils.ErrorCode;
import ai.geteam.client.feign.IamService;
import ai.geteam.client.helper.JwtHelper;
import ai.geteam.client.mapper.ClientAccountInfoMapper;
import ai.geteam.client.mapper.RecruiterMapper;
import ai.geteam.client.mapper.SignatureMapper;
import ai.geteam.client.repository.CompanyRepository;
import ai.geteam.client.repository.RecruiterRepository;
import ai.geteam.client.repository.SignatureRepository;
import ai.geteam.client.service.recruiter.validator.EmailValidator;
import ai.geteam.client.service.token.TokenService;
import ai.geteam.client.utils.MainUtils;
import ai.geteam.client.utils.RecruiterValidation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.account.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruiterServiceImpl implements RecruiterService {

    private final CompanyRepository companyRepository;

    private final EmailService emailService;

    private final EmailValidator emailValidator;

    private final MainUtils mainUtils;

    private final RecruiterRepository recruiterRepository;

    private final RecruiterValidator validator;

    private final TokenService tokenService;

    @Value("${keycloak.realm}")
    public String realm;

    private final SignatureRepository signatureRepository;

    private final JwtHelper jwtHelper;

    private final IamService iamService;

    @Override
    public RecruiterDTO getTeamMember(Long userId) {
        Recruiter recruiter = getRecruiter(userId);
        return RecruiterMapper.toRecruiterDTO(recruiter);
    }

    private Recruiter getRecruiter(Long userId) {
        Optional<Recruiter> recruiterOptional = recruiterRepository.findById(userId);
        if (recruiterOptional.isEmpty()) {
            log.error("User not found");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "User not found");
        }
        return recruiterOptional.get();
    }

    public List<RecruiterDTO> getAllTeamMember() {
        //get Recruiter company
        //retrieve client email from token
        Optional<Recruiter> recruiterOptional = recruiterRepository.findByEmail(getUserEmail());
        if (recruiterOptional.isEmpty()) {
            log.error("failed to find Recruiter by email ");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Recruiter not found");
        }
        if (recruiterOptional.get().getCompany() == null) {
            log.error("Recruiter company not found");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Recruiter company not found");

        }
        List<Recruiter> recruiters = recruiterRepository.findAllByCompany(recruiterOptional.get().getCompany());
        // Convert the recruiters to recruitersDTO
        return recruiters.stream()
                .map(RecruiterMapper::toRecruiterDTO)
                .toList();
    }

    @Override
    public boolean deleteClient(Long clientId, String authorization) {
        final String REALM = "client";

        String currentRecruiterEmail
                = jwtHelper.extractEmail(authorization);

        if (currentRecruiterEmail == null) {
            log.error("Invalid access token: email not found");
            throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, "Invalid access token: email not found");
        }

        // get the recruiter from the database
        Optional<Recruiter> currentRecruiterOptional = recruiterRepository.findByEmail(currentRecruiterEmail);
        if (currentRecruiterOptional.isEmpty()) {
            log.error("Invalid recruiter id " + clientId + ": recruiter not found in the database");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Invalid recruiter id: recruiter not found in the database");
        }
        Recruiter currentRecruiter = currentRecruiterOptional.get();
        // get the company id of the current recruiter
        Long currentRecruiterCompanyId
                = currentRecruiter.getCompany().getId();

        // Check if the current recruiter is Admin
        if (!currentRecruiter.isAdmin()) {
            log.error("Current recruiter is not an admin");
            throw new InvalidInputException(ErrorCode.USER_NOT_ADMIN, "Current recruiter is not an admin");
        }

        Recruiter recruiterToDelete = recruiterRepository.findById(clientId).orElse(null);
        if (recruiterToDelete == null) {
            //mention which id
            log.error("Invalid recruiter id " + clientId + ": recruiter to delete not found in the database");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Invalid recruiter id: recruiter to delete not found in the database");
        }
        //get email of the recruiter to delete
        String recruiterToDeleteEmail = recruiterToDelete.getEmail();

        //get the company id of the recruiter to delete
        Long recruiterToDeleteCompanyId = recruiterToDelete.getCompany().getId();

        // Check if the recruiters belong to the same company
        if (!currentRecruiterCompanyId.equals(recruiterToDeleteCompanyId)) {
            log.error("Recruiters do not belong to the same company");
            throw new InvalidInputException(ErrorCode.USER_NOT_IN_SAME_TEAM, "Recruiters do not belong to the same company");
        }


        List<UserRepresentation> listRecruiterToDeleteFromKeycloak
                = iamService.getUserByUsername(REALM, recruiterToDeleteEmail, authorization);

        if (listRecruiterToDeleteFromKeycloak.isEmpty()) {
            log.error("Invalid recruiter id: recruiter to delete not found in keycloak");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Invalid recruiter id: recruiter to delete not found in keycloak");
        }

        UserRepresentation recruiterToDeleteFromKeycloak
                = listRecruiterToDeleteFromKeycloak.get(0);

        // get keycloak id of the current recruiter
        String recruiterToDeleteId
                = recruiterToDeleteFromKeycloak.getId();
        try {
            //deleteClient from keycloak
            iamService.deleteUser(REALM, recruiterToDeleteId, authorization);

        } catch (Exception e) {
            log.error("Invalid recruiter id // Response from keycloak: {}", e.getMessage());
            throw new ServerException("Invalid recruiter id // Response from keycloak: " + e.getMessage());
        }

        try {
            //delete the recruiter from the database
            recruiterRepository.deleteById(clientId);
        } catch (Exception e) {
            log.error("Invalid recruiter id // Response from database: {}", e.getMessage());
            throw new ServerException("Invalid recruiter id // Response from database: " + e.getMessage());
        }

        return true;
    }

    @Override
    @Transactional
    public void sendInvitation(String authorizationHeader, InvitationEmailRequestDTO invitationRequest) {
        String userEmail = invitationRequest.getEmail();
        validator.verifyIsAdmin(authorizationHeader);
        if (userEmail == null || userEmail.trim().isEmpty()) {
            log.error("The field email is empty");
            throw new InvalidInputException(ErrorCode.EMAIL_EMPTY, "The field email is empty");
        }
        emailValidator.test(userEmail);
        validator.userDoesNotExist(userEmail, authorizationHeader);
        Long companyId = validator.extractCompanyId(authorizationHeader);
        RecruiterDTO newRecruiterDTO = RecruiterDTO.builder()
                .email(userEmail)
                .admin(false)
                .status(Status.INVITED)
                .companyId(companyId)
                .build();
        Long userId = validator.addNewUserToDatabase(newRecruiterDTO);
        emailService.sendInvitationEmail(userEmail, userId);
    }

    @Override
    public ClientAccountInfoDTO getClientPersonalInfo() {
        //retrieve client email from token
        Optional<Recruiter> recruiter = recruiterRepository.findByEmail(getUserEmail());
        if (recruiter.isEmpty()) {
            log.error("failed to find Recruiter by email");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Recruiter not found");
        }
        return ClientAccountInfoMapper.toClientAccountInfo(recruiter.get());

    }

    @Override
    public RecruiterDTO assignAdminRole(Long userID, RoleDTO roleDTO) {
        Recruiter toBeAdmin = recruiterRepository.findById(userID)
                .orElseThrow(() -> new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Utilisateur avec le userId " + userID + " n’existe pas"));
        String principal = mainUtils.getPrincipalMail();
        Recruiter adminRecruiter = recruiterRepository.findByEmail(principal)
                .orElseThrow(() -> new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Utilisateur  n’existe pas"));
        if (!adminRecruiter.getCompany().equals(toBeAdmin.getCompany()))
            throw new InvalidInputException(ErrorCode.USER_NOT_IN_SAME_TEAM, "L’utilisateur avec le userId " + userID + " et l’admin n’existe pas dans la même équipe");
        if (!adminRecruiter.isAdmin())
            throw new InvalidInputException(ErrorCode.USER_NOT_ADMIN, "l'utilisateur qui appelle cette api n'est pas admin ");
        toBeAdmin.setAdmin(roleDTO.getAdmin());
        return RecruiterMapper.toRecruiterDTO(recruiterRepository.save(toBeAdmin));

    }

    @Override
    public void updateRecruiter(String firstName, String lastName, String phone) {
        String email = mainUtils.getPrincipalMail();
        Recruiter recruiter = recruiterRepository.findByEmail(email)
                .orElse(null);

        if (recruiter == null)
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur est survenue, vos informations n'ont pas pu être modifiées");
        Optional<Recruiter> recruiterByPhone = recruiterRepository.findByPhone(phone);
        if (recruiterByPhone.isPresent() && !Objects.equals(recruiterByPhone.get().getId(), recruiter.getId()))
            throw new InvalidInputException(ErrorCode.PHONE_INVALID, "Le numero de téléphone est dèja enregistré pour un autre utilisateur ");
        if (!RecruiterValidation.isValidPhone(phone))
            throw new InvalidInputException(ErrorCode.PHONE_INVALID, "Le numéro de téléphone que vous avez saisi n'est pas un numéro valable");
        if (!RecruiterValidation.isValidName(firstName))
            throw new InvalidInputException(ErrorCode.FIRST_NAME_INVALID, "Le prénom n'est pas valid");
        if (!RecruiterValidation.isValidName(lastName))
            throw new InvalidInputException(ErrorCode.LAST_NAME_INVALID, "Le nom n'est pas valid");
        recruiter.setFirstName(firstName);
        recruiter.setLastName(lastName);
        recruiter.setPhone(phone);
        recruiterRepository.save(recruiter);
    }

    @Override
    public RecruiterDTO activateInvitedClient(Long userId, String status) {

        Optional<Recruiter> optionalClient = recruiterRepository.findById(userId);
        if (optionalClient.isEmpty()) {
            log.error("Client with id: " + userId + " doesn't exist");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "Client not found");
        }

        Recruiter client = optionalClient.get();
        client.setStatus(Status.valueOf(status));
        recruiterRepository.save(client);

        return RecruiterMapper.toRecruiterDTO(client);
    }

    public static String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    //Logic to active a recruiter
    @Override
    @Transactional
    public RecruiterDTO updateRecruiterStatus(Long userId, String authorization) {
        Optional<Recruiter> recruiterOptional = recruiterRepository.findById(userId);
        if (recruiterOptional.isEmpty()) {
            log.error("USER not found");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "USER not found");
        }

        Recruiter recruiter = recruiterOptional.get();

        // 1.verifiez que l'utilisateur est un admin
        validator.validateAdmin(authorization);

        //2. verifiez que l'utilisateur admin est active
        validator.validateStatusActive(authorization);

        // 2.VERIFY THAT THE USERS ON THE SAME TEAM
        validator.validateSameCompany(recruiter, authorization);

        // 3.VERIFY THE STATUS
        validator.validateRecruiterStatus(recruiter);

        //SINON
        recruiter.setStatus(Status.ACTIVE);
        unblockRecruiterInKeycloak(recruiter.getEmail(), authorization);

        Recruiter updatedRecruiter = recruiterRepository.save(recruiter);

        return RecruiterMapper.toRecruiterDTO(updatedRecruiter);
    }

    private void unblockRecruiterInKeycloak(String email, String authorization) {

        ResponseEntity<String> response = iamService.unblockRecruiter(realm, email, authorization);
        if (response.getStatusCode() != HttpStatus.OK) {
            String message = "unable to unblock client";
            log.error(message);
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }

    }

    @Override
    public RecruiterDTO blockRecruiter(Long id, Authentication authentication) {
        // get the authentication token
        log.info("getting authentication token");
        Jwt token = tokenService.getToken(authentication);
        //get the recruiter to block
        Recruiter recruiter = getRecruiter(id);
        log.info("getting the recruiter to block with id : " + recruiter.getId());
        //test if recruiter already blocked
        if (recruiter.getStatus().equals(Status.BLOCKED)) {
            log.error("Client already blocked");
            throw new InvalidInputException(ErrorCode.USER_ALREADY_BLOCKED, "recruiter already blocked");
        }
        //get the admin email to get the recruiter in ms-client database
        String userEmail = (String) token.getClaims().get("email");
        log.info("getting the user email (Admin) : " + userEmail);
        //test if the token has a none empty email
        if (userEmail.isEmpty())
            throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, "There is no email in this token");
        RecruiterDTO admin = getRecruiterByEmail(userEmail);
        log.info("get the admin from database using email");
        //check if the recruiter and admin are the same
        if (admin.getId().equals(recruiter.getId())) {
            log.error("The user and the recruiter to block are the same");
            throw new InvalidInputException(ErrorCode.GENERAL_EXCEPTION, "The user is the recruiter to be blocked");
        }
        if (!admin.isAdmin()) {
            log.error("the user is not an admin, unauthorized action");
            throw new InvalidInputException(ErrorCode.USER_NOT_ADMIN, "User is not an admin");
        }

        //test if the admin and recruiter are in the same team
        if (!((admin.getCompanyId()).equals(recruiter.getCompany().getId()))) {
            log.error("The admin and the user are not in the same team, unauthorized action");
            throw new InvalidInputException(ErrorCode.USER_NOT_IN_SAME_TEAM, "admin and recruiter not in the same team");
        }
        //test if access token is in the keycloak realm 'client'
        if (!(tokenService.getRealm(token).equals("client"))) {
            log.error("the access token is not from 'client' realm");
            throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_NOT_REALM_CLIENT, " user is not in 'client' realm");
        }
        recruiter.setStatus(Status.BLOCKED);
        recruiter = recruiterRepository.save(recruiter);
        blockRecruiterInKeycloak(recruiter.getEmail(), token.getTokenValue());
        return RecruiterMapper.toRecruiterDTO(recruiter);
    }

    private void blockRecruiterInKeycloak(String email, String token) {

        ResponseEntity<String> response = iamService.blockRecruiter(realm, email, "Bearer " + token);
        if (response.getStatusCode() != HttpStatus.OK) {
            String message = "unable to block client";
            log.error(message);
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }
    }

    @Override
    public RecruiterDTO getRecruiterByEmail(String email) {
        Optional<Recruiter> recruiterOptional = recruiterRepository.findByEmail(email);
        return RecruiterMapper.toRecruiterDTO(
                recruiterOptional.orElseThrow(
                        () -> new InvalidInputException(ErrorCode.NO_USER_WITH_THIS_EMAIL, "No user is with this email")
                )
        );
    }

    @Override
    @Transactional
    public SignatureDTO getSignatureById(Long signatureId, String authorization) {
        validator.verifyIsAdmin(authorization);
        return SignatureMapper.toSignatureDTO(
                signatureRepository.findById(signatureId)
                        .orElseThrow(() -> new InvalidInputException(ErrorCode.SIGNATURE_NOT_FOUND, "This signature does not exist"))
        );
    }

    @Override
    public String getClientId(String email) {
        if (!emailValidator.test(email)) {
            String ms = "Invalid email";
            log.error(ms);
            throw new InvalidInputException(ErrorCode.EMAIL_INVALID, ms);
        }
        Recruiter recruiter = recruiterRepository.findByEmail(email).orElseThrow(() -> {
            String ms = "No user is with this email";
            log.error(ms);
            return new InvalidInputException(ErrorCode.NO_USER_WITH_THIS_EMAIL, ms);
        });
        return String.valueOf(recruiter.getCompany().getId());
    }

    @Override
    public SignatureDTO setdefaultSignature(Long signatureId, String authorization) {
        // vérifiez que l'utilisateur est admin
        validator.validateAdmin(authorization);

        // vérifiez que l'utilisateur est active
        validator.validateStatusActive(authorization);

        // vérifiez que la signature Id existe dans la BD
        Optional<Signature> signatureOptional = signatureRepository.findById(signatureId);
        if (!signatureOptional.isPresent()) {
            log.error("La signature avec signatureId n’existe pas");
            throw new InvalidInputException(ErrorCode.SIGNATURE_NOT_FOUND, "La signature avec signatureId n’existe pas.");
        }

        Signature signature = signatureOptional.get();

        // vérifiez que la signature appartient aux meme company que l'utilisateur
        validator.validateSignatureCompany(authorization, signature);

        // récupérez les signatures d'un company qui est par defaut
        List<Signature> defaultSignatures = signatureRepository.findByCompanyIdAndDefaultValueTrue(signature.getCompany().getId());
        /// CHECK si le nombres de signatures par defaut > 1 ==> declenchez un message il doit etre uneseule signature par defaut
        if (defaultSignatures.size() > 1) {
            log.error("Il ne peut y avoir qu'une seule signature par défaut pour une entreprise");

            // corriger et mettez tous les signatures non par defaut
            for (Signature s : defaultSignatures) {
                s.setDefaultValue(false);
                signatureRepository.save(s);
            }
            // Mettez la signature que nous voulons par défaut ayant la signature id
            signature.setDefaultValue(true);
            signatureRepository.save(signature);
            return SignatureMapper.toSignatureDTO(signature);
        }

        // Si le nombre de signatures par défaut est 1
        if (defaultSignatures.size() == 1) {
            // Vérifiez si l'ID de la signature par défaut est similaire de celui spécifié
            if (defaultSignatures.get(0).getId().equals(signatureId)) {
                log.error("La signature est dèja par défaut");
                //throw new InvalidInputException(ErrorCode.)
            } else {
                // Mettez à l'actuel par non par défaut et mettez la signature spécifiée par défaut
                defaultSignatures.get(0).setDefaultValue(false);
                signature.setDefaultValue(true);
                signatureRepository.save(defaultSignatures.get(0));
                signatureRepository.save(signature);
                return SignatureMapper.toSignatureDTO(signature);
            }
        }

        // Si le nombre de signatures par défaut est 0, mettez la signature spécifiée par défaut
        signature.setDefaultValue(true);
        signatureRepository.save(signature);
        return SignatureMapper.toSignatureDTO(signature);
    }
}

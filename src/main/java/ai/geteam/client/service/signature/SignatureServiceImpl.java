package ai.geteam.client.service.signature;

import ai.geteam.client.dto.SignatureDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.entity.signatue.Name;
import ai.geteam.client.entity.signatue.Signature;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.exception.utils.ErrorCode;
import ai.geteam.client.mapper.SignatureMapper;
import ai.geteam.client.repository.RecruiterRepository;
import ai.geteam.client.repository.SignatureRepository;
import ai.geteam.client.service.token.TokenService;
import ai.geteam.client.utils.SignatureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ai.geteam.client.service.recruiter.RecruiterServiceImpl.getUserEmail;


@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureServiceImpl implements SignatureService{
    private final SignatureRepository signatureRepository;
    private final TokenService tokenService;
    private final RecruiterRepository recruiterRepository;
    @Override @Transactional
    public SignatureDTO createSignature(SignatureDTO signatureDTO, Authentication authentication) {
        //Get the authentication token
        Jwt token= tokenService.getToken(authentication);
        log.info("Received access token");
        //get the user email from authentication token
        String userEmail=(String) token.getClaims().get("email");
        log.info("Extracted user email : "+userEmail);
        //get the authenticated user by the email
        Recruiter recruiter=recruiterRepository.findByEmail(userEmail).orElseThrow(()-> new InvalidInputException(ErrorCode.USER_NOT_FOUND,"No user is with this email"));
        log.info("getting the admin using "+userEmail+" email successfully");
        //check if the user is an admin
        if(!recruiter.isAdmin()) {
            log.error("the user is not an admin");
            throw new UnAuthorizedException(ErrorCode.USER_NOT_ADMIN,"The user is not an admin");
        }
        log.info("User is an admin");
        //get the user company
        Company company= recruiter.getCompany();
        log.info("getting the admin's company successfully");
        //check if the signature is Base64format
        if(!isBase64Format(signatureDTO.getValue())){
            log.error(signatureDTO.getValue()+" is not a base64 format");
            throw  new InvalidInputException(ErrorCode.GENERAL_EXCEPTION,"signature is not a base64 format");
        }
        log.info(signatureDTO.getValue()+" is a base64 format");

        SignatureUtils signatureUtils=new SignatureUtils();
        int signatureSize=signatureUtils.getSignatureSize(signatureDTO.getValue());
        if(signatureSize >= 1000000) {
            log.error(" Signature size is more then 1 MB ( "+ signatureSize + " byte)");
            throw new InvalidInputException(ErrorCode.SIGNATURE_SIZE_INVALID,"image size is too big");
        }
        log.info("Size is ok ( "+signatureSize+" )");

        if(!signatureUtils.getSignatureImageExtention(signatureDTO.getValue()).equals("jpg") &&
                !signatureUtils.getSignatureImageExtention(signatureDTO.getValue()).equals("png")){
            throw new InvalidInputException(ErrorCode.SIGNATURE_FORMAT_INVALID,"image format is unacceptable");
        }
        log.info("Extension is ok");
        //creating new empty signature
        Signature signature=new Signature();
        //check the necessary input fields if are null

        if(signatureDTO.getName()==null || signatureDTO.getName().isEmpty()){
            log.error("name is null or empty");
            throw new InvalidInputException(ErrorCode.NAME_EMPTY,"name is null or empty");
        }
        //check if the name is valid (enum)
        if (
                !signatureDTO.getName().equals("TEXT") &&
                !signatureDTO.getName().equals("SIGN") &&
                !signatureDTO.getName().equals("IMAGE")
        ) {
            log.error("The name field must be 'TEXT', 'SIGN' or 'IMAGE'");
            throw new InvalidInputException(ErrorCode.NAME_INVALID,"Signature name is invalid");
        }
        log.info("Signature name field is valid");
        // if(signatureDTO.getValue()==null ) {
        //     log.error("The value of signature is null");
        //     throw new InvalidInputException(ErrorCode.SIGNATURE_EMPTY,"Signature is null");
        // }
        log.info("The signature value is valid");
        //set signature from the signatureDTO input
        log.info("Creating the signature...");
        signature.setName(Enum.valueOf(Name.class,signatureDTO.getName()));
        signature.setValue(signatureDTO.getValue());
        signature.setCompany(company);
        //assign true to default if the signture is the first, false if not
        signature.setDefaultValue(company.getSignatures().isEmpty());
        //saving the signature
        signature=signatureRepository.save(signature);
        log.info("Signature successfully created and well stored");
        return SignatureMapper.toSignatureDTO(signature);

    }

    public String delete(Long id, Authentication authentication) {
        Jwt token = tokenService.getToken(authentication);
        log.info("Received access token");
        String userEmail = (String) token.getClaims().get("email");
        log.info("Extracted user email : " + userEmail);
        Recruiter recruiter = recruiterRepository.findByEmail(userEmail).orElseThrow(() -> new InvalidInputException(ErrorCode.USER_NOT_FOUND, "No user is with this email"));
        log.info("getting the admin using " + userEmail + " email successfully");
        if (!recruiter.isAdmin()) {
            log.error("the user is not an admin");
            throw new UnAuthorizedException(ErrorCode.USER_NOT_ADMIN, "The user is not an admin");
        }
        log.info("User is an admin");
        //get the user company
        Company company = recruiter.getCompany();
        log.info("getting the admin's company successfully");
        List<Signature> signs = signatureRepository.findAllByCompany(company);

        Signature signature = signs.stream().filter(r ->
                        r.getId().equals(id)

                ).findFirst()
                .orElseThrow(() ->
                        new UnAuthorizedException(ErrorCode.SIGNATURE_NOT_FOUND, "invalid signature Id"));
            if(signature.isDefaultValue()){
                throw new InvalidInputException(ErrorCode.SIGNATURE_NOT_DELETED, "cannot delete default signature");
            }

        signatureRepository.deleteById(signature.getId());
        return "Signature deleted successfully";
    }



    public boolean isBase64Format(String string){
        String pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(string);
        return m.find();
    }


    @Override @Transactional
    public List<SignatureDTO> getSignatures() {
        // Check the user by email
        Optional<Recruiter> connectedUser = recruiterRepository.findByEmail(getUserEmail());
        if (connectedUser.isEmpty()) {
            log.error("User with this email does not exist.");
            throw new InvalidInputException(ErrorCode.USER_NOT_FOUND, "User with this email does not exist.");
        }

        // Check that the user is the admin
        if (!connectedUser.get().isAdmin()) {
            log.error("The user who calls this API is not an admin.");
            throw new InvalidInputException(ErrorCode.USER_NOT_ADMIN, "The user who calls this API is not an admin");
        }

        // Retrieve signatures for the user's company
        List<Signature> signatureList = signatureRepository.findAllByCompany(connectedUser.get().getCompany());
        log.info("Retrieved {} signatures for company: {}", signatureList.size(), connectedUser.get().getCompany().getName());

        return signatureList.stream()
                .map(SignatureMapper::toSignatureDTO)
                .toList();
    }




}

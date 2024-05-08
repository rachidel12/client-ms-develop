package ai.geteam.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.dto.SignatureDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.location.City;
import ai.geteam.client.entity.location.Country;
import ai.geteam.client.entity.location.State;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.entity.recruiter.Status;
import ai.geteam.client.entity.signatue.Name;
import ai.geteam.client.entity.signatue.Signature;
import ai.geteam.client.exception.BaseException;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.feign.IamService;
import ai.geteam.client.mapper.RecruiterMapper;
import ai.geteam.client.mapper.SignatureMapper;
import ai.geteam.client.repository.RecruiterRepository;
import ai.geteam.client.repository.SignatureRepository;
import ai.geteam.client.service.recruiter.RecruiterService;
import ai.geteam.client.service.recruiter.RecruiterServiceImpl;
import ai.geteam.client.service.recruiter.RecruiterValidator;
import ai.geteam.client.service.recruiter.validator.EmailValidator;
import ai.geteam.client.service.token.TokenServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RecruiterService2Test {
    

    @Mock
    private RecruiterRepository recruiterRepository;

    @Mock
    private TokenServiceImpl tokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private IamService iamService;

    @Mock 
    private RecruiterValidator validator;   

    @Mock
    private EmailValidator emailValidator;
    
    @Mock
    private SignatureRepository signatureRepository;

    @InjectMocks
    private RecruiterServiceImpl recruiterService;

    private Recruiter recruiter1;
    private Recruiter recruiter2;
    private Recruiter recruiter3;
    private Recruiter recruiter4;
    private RecruiterDTO recruiterDTO1;
    private RecruiterDTO recruiterDTO2;
    private RecruiterDTO recruiterDTO3;
    private Company company1;
    private Company company2;
    private Country country1;
    private State state1;
    private City city1;

    @BeforeEach
    public void setUp() {
        country1 = new Country(1L, "Country Test 1", "CT1", null, null);
        state1 = new State(1L, "State Test 1", "ST1", country1);
        city1 = new City(1L, "City Test 1", "CT1", country1, state1);
        company1 = new Company(1L, "Company Test 1", "www.companytest1.com", "size1", null, null, country1, state1, city1, null, null);
        company2 = new Company(2L, "Company Test 2", "www.companytest2.com", "size2", null, null, null, null, null, null, null);
        recruiter1 = new Recruiter(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", true, "0606060606", company1, Status.ACTIVE);
        recruiter2 = new Recruiter(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", company1, Status.BLOCKED);
        recruiter3 = new Recruiter(3L, "recruiterFI3", "recruiterLA3", "recruiter3@gmail.com", false, "0606060606", company2, Status.ACTIVE);
        recruiter4 = new Recruiter(4L, "recruiterFI4", "recruiterLA4", "recruiter4@gmail.com", true, "0606060606", company1, Status.ACTIVE);
        recruiterDTO1 = new RecruiterDTO(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", true, "0606060606", 1L, Status.ACTIVE);
        recruiterDTO2 = new RecruiterDTO(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", 1L, Status.BLOCKED);
        recruiterDTO3 = new RecruiterDTO(3L, "recruiterFI3", "recruiterLA3", "recruiter3@gmail.com", false, "0606060606", 2L, Status.ACTIVE);
        // MockitoAnnotations.openMocks(this);
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: User not found")
    void RecruiterService_blockRecruiter_ReturnsNotFound(){
        // Arrange
        Long userId = 99L;
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter2@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.empty());
        //Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: User is already blocked")
    void RecruiterService_blockRecruiter_ReturnsAlreadyBlocked(){
        // Arrange
        Long userId = 2L;
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter1@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter2));
        //Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: token has an empty email")
    void RecruiterService_blockRecruiter_ReturnsEmptyEmail(){
        // Arrange
        Long userId = 1L;
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter1));
        //Act
        assertThrows(UnAuthorizedException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: No recruiter is with this email")
    void RecruiterService_blockRecruiter_ReturnsNoEmailRecruiter(){
        // Arrange
        Long userId = 1L;
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter99@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByEmail("recruiter99@gmail.com")).thenReturn(Optional.empty());
        //Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: The recruiter and admin are the same")
    void RecruiterService_blockRecruiter_ReturnsSameAdminRecruiter(){
        // Arrange
        Long userId = 1L;
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter1@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        //Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: The user authenticated is not an admin")
    void RecruiterService_blockRecruiter_ReturnsNoAdin(){
        // Arrange
        Long userId = 1L;
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter2@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByEmail("recruiter2@gmail.com")).thenReturn(Optional.of(recruiter2));
        //Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: The user and recruiter not in the same company")
    void RecruiterService_blockRecruiter_ReturnsNotSameCompany(){
        // Arrange
        Long userId = 3L;
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter1@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter3));
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        //Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: The token not in realm client")
    void RecruiterService_blockRecruiter_ReturnsNotRealmClient(){
        // Arrange
        Long userId = 1L;
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter4@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByEmail("recruiter4@gmail.com")).thenReturn(Optional.of(recruiter4));
        when(tokenService.getRealm(token)).thenReturn("realmNotClient");
        //Act
        assertThrows(UnAuthorizedException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Negative case: Unable to block in keycloak")
    void RecruiterService_blockRecruiter_ReturnsPbInKeycloak(){
        // Arrange
        Long userId = 1L;
        Recruiter recruiterUpdated = new Recruiter(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", true, "0606060606", company1, Status.BLOCKED);
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter4@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        ResponseEntity<String> response = ResponseEntity.badRequest().build();
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByEmail("recruiter4@gmail.com")).thenReturn(Optional.of(recruiter4));
        when(tokenService.getRealm(token)).thenReturn("client");
        when(recruiterRepository.save(recruiter1)).thenReturn(recruiterUpdated);
        when(iamService.blockRecruiter(null, "recruiter1@gmail.com", "Bearer "+token.getTokenValue())).thenReturn(response);
        //Act
        assertThrows(BaseException.class, ()->
            recruiterService.blockRecruiter(userId, authentication));
        // Assert
        verify(recruiterRepository,times(1)).save(any());
    }

    @Test
    @Tag("blockRecruiter")
    @DisplayName("blockRecruiter Positive case")
    void RecruiterService_blockRecruiter_ReturnsBlockedRecruiter(){
        // Arrange
        Long userId = 1L;
        Recruiter recruiterUpdated = new Recruiter(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", true, "0606060606", company1, Status.BLOCKED);
        RecruiterDTO recruiterUpdatedDTO = RecruiterMapper.toRecruiterDTO(recruiterUpdated);
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter4@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        ResponseEntity<String> response = ResponseEntity.ok().build();
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByEmail("recruiter4@gmail.com")).thenReturn(Optional.of(recruiter4));
        when(tokenService.getRealm(token)).thenReturn("client");
        when(recruiterRepository.save(recruiter1)).thenReturn(recruiterUpdated);
        when(iamService.blockRecruiter(null, "recruiter1@gmail.com", "Bearer "+token.getTokenValue())).thenReturn(response);
        //Act
        RecruiterDTO result = recruiterService.blockRecruiter(userId, authentication);
        // Assert
        assertEquals(recruiterUpdatedDTO, result);
        verify(recruiterRepository,times(1)).save(any());
    }

    @Test
    @Tag("getSignatureById")
    @DisplayName("getSignatureById Positive case")
    void RecruiterService_getSignatureById_ReturnsSignature(){
        // Arrange
        String authorization = "Bearer token";
        Long signatureId = 1L;
        byte[] signature = new byte[10];
        Signature signatureEntity = new Signature(signatureId, Name.TEXT, "signature", true, company1);
        SignatureDTO signatureDTO = SignatureMapper.toSignatureDTO(signatureEntity);
        when(validator.verifyIsAdmin(authorization)).thenReturn(true);
        when(signatureRepository.findById(signatureId)).thenReturn(Optional.of(signatureEntity));
        //Act
        SignatureDTO result = recruiterService.getSignatureById(signatureId, authorization);
        // Assert
        assertEquals(signatureDTO, result);
    }

    @Test
    @Tag("getSignatureById")
    @DisplayName("getSignatureById Negative case: No signature Found")
    void RecruiterService_getSignatureById_ReturnsNotFound(){
        // Arrange
        String authorization = "Bearer token";
        Long signatureId = 1L;
        Signature signatureEntity = new Signature(signatureId, Name.TEXT, "signature", true, company1);
        SignatureDTO signatureDTO = SignatureMapper.toSignatureDTO(signatureEntity);
        when(validator.verifyIsAdmin(authorization)).thenReturn(true);
        when(signatureRepository.findById(signatureId)).thenReturn(Optional.empty());
        //Act // Assert
        assertThrows(InvalidInputException.class, ()->
            recruiterService.getSignatureById(signatureId, authorization));     
    }
    
    @Test
    @Tag("getClientId")
    @DisplayName("getClientId Positive case")
    void RecruiterService_getClientId_ReturnsSignature(){
        // Arrange
        String email = "recruiter1@gmail.com";
        when(emailValidator.test(email)).thenReturn(true);
        when(recruiterRepository.findByEmail(email)).thenReturn(Optional.of(recruiter1));
        //Act
        String result = recruiterService.getClientId(email);
        // Assert
        assertEquals("1", result);
    }

    @Test
    @Tag("getClientId")
    @DisplayName("getClientId Negative case: Invalid Email")
    void RecruiterService_getClientId_ReturnsInvalidEmail(){
        // Arrange
        String email = "recruiter1@gmail.com";
        when(emailValidator.test(email)).thenReturn(false);
        //Act & Assert
        assertThrows(InvalidInputException.class, ()->
            recruiterService.getClientId(email));

        verify(recruiterRepository, times(0)).findByEmail(email);
    }

    @Test
    @Tag("getClientId")
    @DisplayName("getClientId Negative case: No user with this Email")
    void RecruiterService_getClientId_ReturnsNoUser(){
        // Arrange
        String email = "recruiter1@gmail.com";
        when(emailValidator.test(email)).thenReturn(true);
        when(recruiterRepository.findByEmail(email)).thenReturn(Optional.empty());
        //Act & Assert
        assertThrows(InvalidInputException.class, ()->
            recruiterService.getClientId(email));
        
        verify(recruiterRepository, times(1)).findByEmail(email);
    }

    @Test
    @Tag("setdefaultSignature")
    @DisplayName("setdefaultSignature Negative case: SignatureId doesn't exist")
    void RecruiterService_setdefaultSignature_ReturnsNoSignature(){
        // Arrange
        String authorization = "Bearer token";
        Long signatureId = 99L;
        doNothing().when(validator).validateAdmin(authorization);
        doNothing().when(validator).validateStatusActive(authorization);
        when(signatureRepository.findById(signatureId)).thenReturn(Optional.empty());
        //Act
        assertThrows(InvalidInputException.class, ()->
                    recruiterService.setdefaultSignature(signatureId, authorization));
        // Assert
        verify(signatureRepository, times(0)).save(any());
    }

    @Test
    @Tag("setdefaultSignature")
    @DisplayName("setdefaultSignature Positive case: Multiple default signatures exist")
    void RecruiterService_setdefaultSignature_ReturnsSignatureMulti(){
        // Arrange
        String authorization = "Bearer token";
        Long signatureId = 1L;
        Signature signatureEntity1 = new Signature(signatureId, Name.TEXT, "signature1", true, company1);
        Signature signatureEntity2 = new Signature(2L, Name.TEXT, "signature2", true, company1);
        SignatureDTO signatureDTO1 = new SignatureDTO(signatureId, "TEXT", "signature1", true);
        List<Signature> signatures = new ArrayList<>();
        signatures.add(signatureEntity1);
        signatures.add(signatureEntity2);
        doNothing().when(validator).validateAdmin(authorization);
        doNothing().when(validator).validateStatusActive(authorization);
        doNothing().when(validator).validateSignatureCompany(authorization, signatureEntity1);
        when(signatureRepository.findById(signatureId)).thenReturn(Optional.of(signatureEntity1));
        when(signatureRepository.findByCompanyIdAndDefaultValueTrue(1L)).thenReturn(signatures);
        when(signatureRepository.save(any())).thenReturn(null);

        //Act
        SignatureDTO result = recruiterService.setdefaultSignature(signatureId, authorization);
        // Assert
        assertEquals(signatureDTO1, result);
        verify(signatureRepository, times(3)).save(any());
    }

    @Test
    @Tag("setdefaultSignature")
    @DisplayName("setdefaultSignature Positive case: One default signature already exists")
    void RecruiterService_setdefaultSignature_ReturnsSignatureOne(){
        // Arrange
        String authorization = "Bearer token";
        Signature signatureEntity1 = new Signature(1L, Name.TEXT, "signature1", true, company1);
        Signature signatureEntity2 = new Signature(2L, Name.TEXT, "signature2", false, company1);
        SignatureDTO signatureDTO2 = new SignatureDTO(2L, "TEXT", "signature2", true);
        List<Signature> signatures = new ArrayList<>();
        signatures.add(signatureEntity1);
        doNothing().when(validator).validateAdmin(authorization);
        doNothing().when(validator).validateStatusActive(authorization);
        doNothing().when(validator).validateSignatureCompany(authorization, signatureEntity2);
        when(signatureRepository.findById(2L)).thenReturn(Optional.of(signatureEntity2));
        when(signatureRepository.findByCompanyIdAndDefaultValueTrue(1L)).thenReturn(signatures);
        when(signatureRepository.save(any())).thenReturn(null);

        //Act
        SignatureDTO result = recruiterService.setdefaultSignature(2L, authorization);
        // Assert
        assertEquals(signatureDTO2, result);
        verify(signatureRepository, times(2)).save(any());
    }

    @Test
    @Tag("setdefaultSignature")
    @DisplayName("setdefaultSignature Negative case: Set the default signature to default")
    void RecruiterService_setdefaultSignature_ReturnsSameDefault(){
        // Arrange
        String authorization = "Bearer token";
        Signature signatureEntity1 = new Signature(1L, Name.TEXT, "signature1", true, company1);
        SignatureDTO signatureDTO1 = new SignatureDTO(1L, "TEXT", "signature1", true);
        List<Signature> signatures = new ArrayList<>();
        signatures.add(signatureEntity1);
        doNothing().when(validator).validateAdmin(authorization);
        doNothing().when(validator).validateStatusActive(authorization);
        doNothing().when(validator).validateSignatureCompany(authorization, signatureEntity1);
        when(signatureRepository.findById(1L)).thenReturn(Optional.of(signatureEntity1));
        when(signatureRepository.findByCompanyIdAndDefaultValueTrue(1L)).thenReturn(signatures);
        when(signatureRepository.save(any())).thenReturn(null);

        //Act
        SignatureDTO result = recruiterService.setdefaultSignature(1L, authorization);
        // Assert
        assertEquals(signatureDTO1, result);
        verify(signatureRepository, times(1)).save(any());
    }

    @Test
    @Tag("setdefaultSignature")
    @DisplayName("setdefaultSignature Positive case")
    void RecruiterService_setdefaultSignature_ReturnsDefaultSignature(){
        // Arrange
        String authorization = "Bearer token";
        Signature signatureEntity1 = new Signature(1L, Name.TEXT, "signature1", false, company1);
        SignatureDTO signatureDTO1 = new SignatureDTO(1L, "TEXT", "signature1", true);
        List<Signature> signatures = new ArrayList<>();
        doNothing().when(validator).validateAdmin(authorization);
        doNothing().when(validator).validateStatusActive(authorization);
        doNothing().when(validator).validateSignatureCompany(authorization, signatureEntity1);
        when(signatureRepository.findById(1L)).thenReturn(Optional.of(signatureEntity1));
        when(signatureRepository.findByCompanyIdAndDefaultValueTrue(1L)).thenReturn(signatures);
        when(signatureRepository.save(any())).thenReturn(null);

        //Act
        SignatureDTO result = recruiterService.setdefaultSignature(1L, authorization);
        // Assert
        assertEquals(signatureDTO1, result);
        verify(signatureRepository, times(1)).save(any());
    }
}

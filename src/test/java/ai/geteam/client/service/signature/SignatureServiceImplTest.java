package ai.geteam.client.service.signature;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StreamUtils;

import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.dto.SignatureDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.location.City;
import ai.geteam.client.entity.location.Country;
import ai.geteam.client.entity.location.State;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.entity.recruiter.Status;
import ai.geteam.client.entity.signatue.Signature;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.mapper.SignatureMapper;
import ai.geteam.client.repository.RecruiterRepository;
import ai.geteam.client.repository.SignatureRepository;
import ai.geteam.client.service.token.TokenService;
import ai.geteam.client.utils.SignatureUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class SignatureServiceImplTest {

    @Mock
    private SignatureRepository signatureRepository;

    @Mock
    private TokenService tokenService;
    
    @Mock
    private RecruiterRepository recruiterRepository;

    @Mock
    private Authentication authentication;

    // @Mock
    // private SignatureUtils signatureUtils;

    @InjectMocks
    private SignatureServiceImpl signatureService;

    private Recruiter recruiter1;
    private Recruiter recruiter2;
    private Recruiter recruiter3;
    // private RecruiterDTO recruiterDTO1;
    // private RecruiterDTO recruiterDTO2;
    // private RecruiterDTO recruiterDTO3;
    private Company company1;
    private Company company2;
    private Country country1;
    private State state1;
    private City city1;
    private SignatureDTO signatureDTO1;

    // @Value("classpath:./signaturebase64.txt")
    // org.apache.tomcat.util.file.ConfigurationSource.Resource resource;
    
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
        // recruiterDTO1 = new RecruiterDTO(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", true, "0606060606", 1L, Status.ACTIVE);
        // recruiterDTO2 = new RecruiterDTO(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", 1L, Status.BLOCKED);
        // recruiterDTO3 = new RecruiterDTO(3L, "recruiterFI3", "recruiterLA3", "recruiter3@gmail.com", false, "0606060606", 2L, Status.ACTIVE);
        signatureDTO1 = new SignatureDTO(1L, "test name", "SGVsbG8gV29ybGQh", true);
    }

    @Test
    @Tag("createSignature")
    @DisplayName("createSignature Negative Test: The Recruiter is not found")
    void SignatureService_createSignature_ReturnsNotFound() {
        // Arrange //Already setup
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter99@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findByEmail("recruiter99@gmail.com")).thenReturn(Optional.empty());

        // Act //Assert
        assertThrows(InvalidInputException.class, ()->
                    signatureService.createSignature(signatureDTO1, authentication)
        );
        verify(signatureRepository, times(0)).save(any());
    }

    @Test
    @Tag("createSignature")
    @DisplayName("createSignature Negative Test: The User is not an Admin")
    void SignatureService_createSignature_ReturnsNotAdmin() {
        // Arrange //Already setup
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter2@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findByEmail("recruiter2@gmail.com")).thenReturn(Optional.of(recruiter2));

        // Act //Assert
        assertThrows(UnAuthorizedException.class, ()->
                    signatureService.createSignature(signatureDTO1, authentication)
        );
        verify(signatureRepository, times(0)).save(any());
    }

    @Test
    @Tag("createSignature")
    @DisplayName("createSignature Negative Test: The Signature isn't in Base64 format")
    void SignatureService_createSignature_ReturnsNotBase64() {
        // Arrange 
        SignatureDTO signatureDTO2 = new SignatureDTO(1L, "test name", "test value", true);
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter1@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));

        // Act //Assert
        assertThrows(InvalidInputException.class, ()->
                    signatureService.createSignature(signatureDTO2, authentication)
        );
        verify(signatureRepository, times(0)).save(any());
    }

    @Test
    @Tag("createSignature")
    @DisplayName("createSignature Negative Test: The Signature has exceeded its limit")
    void SignatureService_createSignature_ReturnsSizeMax() throws IOException {
        // Arrange 
        String largeSignatureValue;
        InputStream is = getClass().getClassLoader().getResourceAsStream("signaturebase64.txt");
        largeSignatureValue = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        SignatureDTO signatureDTO2 = new SignatureDTO(1L,"name test", largeSignatureValue, true);
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter1@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        // Act //Assert
        assertThrows(InvalidInputException.class, ()->
                    signatureService.createSignature(signatureDTO2, authentication)
        );
        verify(signatureRepository, times(0)).save(any());
    }

    @Test
    @Tag("createSignature")
    @DisplayName("createSignature Negative Test: The Signature has a null name")
    void SignatureService_createSignature_ReturnsNullName() throws IOException{
        //Arrange 
        String largeSignatureValue;
        // ClassPathResource resource = new ClassPathResource("C:/dna internship/me/client-ms-develop/src/test/java/ai/geteam/resources/signaturebase64.txt");
        InputStream is = getClass().getClassLoader().getResourceAsStream("image64.txt");
        largeSignatureValue = StreamUtils.copyToString(is, StandardCharsets.UTF_8);        
        SignatureDTO signatureDTO2 = new SignatureDTO(1L, null, largeSignatureValue, true);
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter1@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        // Act //Assert
        assertThrows(InvalidInputException.class, ()->
                    signatureService.createSignature(signatureDTO2, authentication)
        );
        verify(signatureRepository, times(0)).save(any());
    }

    @Test
    @Tag("createSignature")
    @DisplayName("createSignature Negative Test: The Signature not have a text name")
    void SignatureService_createSignature_ReturnsNotText() throws IOException{
        //Arrange 
        String largeSignatureValue;
        // ClassPathResource resource = new ClassPathResource("C:/dna internship/me/client-ms-develop/src/test/java/ai/geteam/resources/signaturebase64.txt");
        InputStream is = getClass().getClassLoader().getResourceAsStream("image64.txt");
        largeSignatureValue = StreamUtils.copyToString(is, StandardCharsets.UTF_8);        
        SignatureDTO signatureDTO2 = new SignatureDTO(1L, "test name", largeSignatureValue, true);
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter1@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        // Act //Assert
        assertThrows(InvalidInputException.class, ()->
                    signatureService.createSignature(signatureDTO2, authentication)
        );
        verify(signatureRepository, times(0)).save(any());
    }

    @Test
    @Tag("createSignature")
    @DisplayName("createSignature Positive Test")
    void SignatureService_createSignature_ReturnsSignature() throws IOException{
        //Arrange 
        //Arrange 
        String largeSignatureValue;
        // ClassPathResource resource = new ClassPathResource("C:/dna internship/me/client-ms-develop/src/test/java/ai/geteam/resources/signaturebase64.txt");
        InputStream is = getClass().getClassLoader().getResourceAsStream("image64.txt");
        largeSignatureValue = StreamUtils.copyToString(is, StandardCharsets.UTF_8);        
        SignatureDTO signatureDTO2 = new SignatureDTO(1L, "IMAGE", largeSignatureValue, true);
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "recruiter1@gmail.com");
        Jwt token = new Jwt("token", null, null, headers, claims);
        Signature signature = SignatureMapper.toSignature(signatureDTO2);
        signature.setCompany(company1);
        List<Signature> signatures = new ArrayList<>();
        company1.setSignatures(signatures);
        when(tokenService.getToken(authentication)).thenReturn(token);
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        when(signatureRepository.save(any())).thenReturn(signature);
        // Act //Assert
        SignatureDTO result = signatureService.createSignature(signatureDTO2, authentication);
        
        assertEquals(signatureDTO2, result);
        verify(signatureRepository, times(1)).save(any());
    }
}

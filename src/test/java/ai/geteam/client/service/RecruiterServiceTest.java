package ai.geteam.client.service;


import ai.geteam.client.dto.ClientAccountInfoDTO;
import ai.geteam.client.dto.InvitationEmailRequestDTO;
import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.location.City;
import ai.geteam.client.entity.location.Country;
import ai.geteam.client.entity.location.State;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.repository.RecruiterRepository;
import ai.geteam.client.service.recruiter.EmailService;
import ai.geteam.client.service.recruiter.RecruiterService;
import ai.geteam.client.service.recruiter.RecruiterServiceImpl;
import ai.geteam.client.service.recruiter.RecruiterValidator;
import ai.geteam.client.service.recruiter.validator.EmailValidator;
import io.swagger.v3.oas.models.servers.Server;
import ai.geteam.client.entity.recruiter.Status;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.ServerException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.feign.IamService;
import ai.geteam.client.helper.JwtHelper;
import ai.geteam.client.mapper.ClientAccountInfoMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.account.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecruiterServiceTest {

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private RecruiterRepository recruiterRepository;

    @Mock
    private IamService iamService;

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private RecruiterValidator validator;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailValidator emailValidator;

    @InjectMocks
    private RecruiterServiceImpl recruiterService;

    private Recruiter recruiter1;
    private Recruiter recruiter2;
    private Recruiter recruiter3;
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
        recruiter1 = new Recruiter(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", false, "0606060606", company1, Status.ACTIVE);
        recruiter2 = new Recruiter(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", company1, Status.ACTIVE);
        recruiter3 = new Recruiter(3L, "recruiterFI3", "recruiterLA3", "recruiter3@gmail.com", false, "0606060606", company2, Status.ACTIVE);
        recruiterDTO1 = new RecruiterDTO(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", false, "0606060606", 1L, Status.ACTIVE);
        recruiterDTO2 = new RecruiterDTO(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", 1L, Status.ACTIVE);
        recruiterDTO3 = new RecruiterDTO(3L, "recruiterFI3", "recruiterLA3", "recruiter3@gmail.com", false, "0606060606", 2L, Status.ACTIVE);
        
        // MockitoAnnotations.openMocks(this);
        
    }

    @Test
    @Tag("GetTeamMember")
    @DisplayName("GetTeamMember")
    void RecruiterService_getTeamMember_ReturnsRecruiter() {
        // Arrange
        when(recruiterRepository.findById(1L)).thenReturn(Optional.of(recruiter1));
        // Act
        RecruiterDTO result = recruiterService.getTeamMember(1L);
        // Assert
        assertEquals(recruiterDTO1, result);
        verify(recruiterRepository, times(1)).findById(1L);
    }

    @Test
    @Tag("GetTeamMember")
    @DisplayName("GetTeamMember")
    void RecruiterService_getTeamMember_ReturnsNone() {
        // Arrange
        when(recruiterRepository.findById(99L)).thenReturn(Optional.empty());
        // Act
        // Assert
        assertThrows(InvalidInputException.class,() -> {
            recruiterService.getTeamMember(99L);
        });
        verify(recruiterRepository, times(1)).findById(99L);
    }

    @Test
    @Tag("GetAllTeamMember")
    @DisplayName("GetAllTeamMember")
    void RecruiterService_GetAllTeamMember_ReturnsRecruiters(){
        // Arrange
        // Mocking SecurityContextHolder
        // SecurityContext securityContext = mock(SecurityContext.class);
        // authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("recruiter1@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        List<Recruiter> recruiters = new ArrayList<>();
        recruiters.add(recruiter1);
        recruiters.add(recruiter2);
        when(recruiterRepository.findAllByCompany(company1)).thenReturn(recruiters);

        // Act
        List<RecruiterDTO> result = recruiterService.getAllTeamMember();
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(recruiterDTO1, recruiterDTO2);
        verify(recruiterRepository, times(1)).findAllByCompany(company1);
    }

    @Test
    @Tag("GetAllTeamMember")
    @DisplayName("GetAllTeamMember")
    void RecruiterService_GetAllTeamMember_ReturnsNoRecruiter(){
        // Arrange
        when(authentication.getName()).thenReturn("recruiter1@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.empty());
        // Act
        // Assert
        assertThrows(InvalidInputException.class,() -> {
            recruiterService.getAllTeamMember();
        });
        verify(recruiterRepository, times(0)).findAllByCompany(company1);
    }
    
    @Test
    @Tag("GetAllTeamMember")
    @DisplayName("GetAllTeamMember")
    void RecruiterService_GetAllTeamMember_ReturnsNoCompany(){
        // Arrange
        Recruiter recruiterTestNoCompany = new Recruiter(1L, "recruiterFI1", "recruiterLA1", "recruiter99@gmail.com", false, "0606060606", null, Status.ACTIVE);

        when(authentication.getName()).thenReturn("recruiter99@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(recruiterRepository.findByEmail("recruiter99@gmail.com")).thenReturn(Optional.of(recruiterTestNoCompany));
        // Act
        // Assert
        assertThrows(InvalidInputException.class,() -> {
            recruiterService.getAllTeamMember();
        });
        verify(recruiterRepository, times(0)).findAllByCompany(company1);
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsDeleted(){
        // Arrange
        String authorization = "Bearer token";
        String recruiterEmail = "admin@example.com";
        String recruiterToDeleteEmail="toDelete@example.com";
        Long recruiterCompanyId = 1L;
        Long clientId = 123L;

        Recruiter currentRecruiter = new Recruiter();
        currentRecruiter.setEmail(recruiterEmail);
        currentRecruiter.setAdmin(true);
        Company company = new Company();
        company.setId(recruiterCompanyId);
        currentRecruiter.setCompany(company);

        Recruiter recruiterToDelete = new Recruiter();
        recruiterToDelete.setEmail(recruiterToDeleteEmail);
        recruiterToDelete.setId(clientId);
        Company deleteCompany = new Company();
        deleteCompany.setId(recruiterCompanyId); // Same company as the current recruiter
        recruiterToDelete.setCompany(deleteCompany);

        when(jwtHelper.extractEmail(authorization)).thenReturn(recruiterEmail);

        // Mocking behavior of repository methods
        when(recruiterRepository.findByEmail(recruiterEmail)).thenReturn(Optional.of(currentRecruiter));
        when(recruiterRepository.findById(clientId)).thenReturn(Optional.of(recruiterToDelete));
        // when(iamService.deleteUser(anyString(), anyString(), anyString())).thenReturn(ResponseEntity.ok().build());
        
        // Mocking behavior of IAM service
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("123456");
        when(iamService.getUserByUsername(anyString(), anyString(), anyString())).thenReturn(List.of(userRepresentation));

        // Act
        boolean result = recruiterService.deleteClient(clientId, authorization);

        // Assert
        assertTrue(result);
        verify(iamService).deleteUser(anyString(), eq("123456"), eq(authorization));
        verify(recruiterRepository).deleteById(clientId);
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsEmailnotFound(){
        // assert
        String authorization = "Bearer token";
        when(jwtHelper.extractEmail(authorization)).thenReturn(null);
        // Act & Assert
        assertThrows(UnAuthorizedException.class, () -> 
            recruiterService.deleteClient(123L, authorization));
            
        verify(recruiterRepository, times(0)).deleteById(null);
        verify(iamService, times(0)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsNoRecruiter(){
        String authorization = "Bearer token";
        String recruiterEmail = "admin@example.com";

        when(jwtHelper.extractEmail(authorization)).thenReturn(recruiterEmail);
        when(recruiterRepository.findByEmail(recruiterEmail)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> 
            recruiterService.deleteClient(123L, authorization));
            
        verify(recruiterRepository, times(0)).deleteById(null);
        verify(iamService, times(0)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsNotAdmin(){
        String authorization = "Bearer token";
        String recruiterEmail = "admin@example.com";
        Long recruiterCompanyId = 1L;

        Recruiter currentRecruiter = new Recruiter();
        currentRecruiter.setEmail(recruiterEmail);
        currentRecruiter.setAdmin(false);
        Company company = new Company();
        company.setId(recruiterCompanyId);
        currentRecruiter.setCompany(company);

        when(jwtHelper.extractEmail(authorization)).thenReturn(recruiterEmail);
        when(recruiterRepository.findByEmail(recruiterEmail)).thenReturn(Optional.of(currentRecruiter));
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> 
            recruiterService.deleteClient(123L, authorization));
            
        verify(recruiterRepository, times(0)).deleteById(null);
        verify(iamService, times(0)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsRecruiterToDeleeteNotFound(){
        String authorization = "Bearer token";
        String recruiterEmail = "admin@example.com";
        String recruiterToDeleteEmail="toDelete@example.com";
        Long recruiterCompanyId = 1L;
        Long clientId = 123L;

        Recruiter currentRecruiter = new Recruiter();
        currentRecruiter.setEmail(recruiterEmail);
        currentRecruiter.setAdmin(true);
        Company company = new Company();
        company.setId(recruiterCompanyId);
        currentRecruiter.setCompany(company);

        when(jwtHelper.extractEmail(authorization)).thenReturn(recruiterEmail);
        when(recruiterRepository.findByEmail(recruiterEmail)).thenReturn(Optional.of(currentRecruiter));
        when(recruiterRepository.findById(clientId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> 
            recruiterService.deleteClient(123L, authorization));
            
        verify(recruiterRepository, times(1)).findById(anyLong());
        verify(recruiterRepository, times(0)).deleteById(null);
        verify(iamService, times(0)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsNotSameCompany(){
        String authorization = "Bearer token";
        String recruiterEmail = "admin@example.com";
        String recruiterToDeleteEmail="toDelete@example.com";
        Long recruiterCompanyId = 1L;
        Long clientId = 123L;

        Recruiter currentRecruiter = new Recruiter();
        currentRecruiter.setEmail(recruiterEmail);
        currentRecruiter.setAdmin(true);
        Company company = new Company();
        company.setId(recruiterCompanyId);
        currentRecruiter.setCompany(company);

        Recruiter recruiterToDelete = new Recruiter();
        recruiterToDelete.setEmail(recruiterToDeleteEmail);
        recruiterToDelete.setId(clientId);
        Company deleteCompany = new Company();
        deleteCompany.setId(2L); // Same company as the current recruiter
        recruiterToDelete.setCompany(deleteCompany);

        when(jwtHelper.extractEmail(authorization)).thenReturn(recruiterEmail);
        when(recruiterRepository.findByEmail(recruiterEmail)).thenReturn(Optional.of(currentRecruiter));
        when(recruiterRepository.findById(clientId)).thenReturn(Optional.of(recruiterToDelete));
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> 
            recruiterService.deleteClient(123L, authorization));
            
        verify(recruiterRepository, times(1)).findById(anyLong());
        verify(recruiterRepository, times(0)).deleteById(null);
        verify(iamService, times(0)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsNotFoundInKeycloak(){
        String authorization = "Bearer token";
        String recruiterEmail = "admin@example.com";
        String recruiterToDeleteEmail="toDelete@example.com";
        Long recruiterCompanyId = 1L;
        Long clientId = 123L;

        Recruiter currentRecruiter = new Recruiter();
        currentRecruiter.setEmail(recruiterEmail);
        currentRecruiter.setAdmin(true);
        Company company = new Company();
        company.setId(recruiterCompanyId);
        currentRecruiter.setCompany(company);

        Recruiter recruiterToDelete = new Recruiter();
        recruiterToDelete.setEmail(recruiterToDeleteEmail);
        recruiterToDelete.setId(clientId);
        Company deleteCompany = new Company();
        deleteCompany.setId(recruiterCompanyId); // Same company as the current recruiter
        recruiterToDelete.setCompany(deleteCompany);

        when(jwtHelper.extractEmail(authorization)).thenReturn(recruiterEmail);
        when(recruiterRepository.findByEmail(recruiterEmail)).thenReturn(Optional.of(currentRecruiter));
        when(recruiterRepository.findById(clientId)).thenReturn(Optional.of(recruiterToDelete));
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("123456");
        when(iamService.getUserByUsername(anyString(), anyString(), anyString())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> 
            recruiterService.deleteClient(123L, authorization));
        
        verify(recruiterRepository, times(1)).findById(anyLong());
        verify(recruiterRepository, times(0)).deleteById(null);
        verify(iamService, times(0)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsExceptionIamService(){
        String authorization = "Bearer token";
        String recruiterEmail = "admin@example.com";
        String recruiterToDeleteEmail="toDelete@example.com";
        Long recruiterCompanyId = 1L;
        Long clientId = 123L;

        Recruiter currentRecruiter = new Recruiter();
        currentRecruiter.setEmail(recruiterEmail);
        currentRecruiter.setAdmin(true);
        Company company = new Company();
        company.setId(recruiterCompanyId);
        currentRecruiter.setCompany(company);

        Recruiter recruiterToDelete = new Recruiter();
        recruiterToDelete.setEmail(recruiterToDeleteEmail);
        recruiterToDelete.setId(clientId);
        Company deleteCompany = new Company();
        deleteCompany.setId(recruiterCompanyId); // Same company as the current recruiter
        recruiterToDelete.setCompany(deleteCompany);

        when(jwtHelper.extractEmail(authorization)).thenReturn(recruiterEmail);
        when(recruiterRepository.findByEmail(recruiterEmail)).thenReturn(Optional.of(currentRecruiter));
        when(recruiterRepository.findById(clientId)).thenReturn(Optional.of(recruiterToDelete));
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("123456");
        when(iamService.getUserByUsername(anyString(), anyString(), anyString())).thenReturn(List.of(userRepresentation));
        when(iamService.deleteUser(anyString(), anyString(), anyString())).thenThrow(new ServerException("Error in IAM service"));

        // Act & Assert
        assertThrows(ServerException.class, () -> 
            recruiterService.deleteClient(123L, authorization));
        
        verify(recruiterRepository, times(1)).findById(anyLong());
        verify(recruiterRepository, times(0)).deleteById(null);
        verify(iamService, times(1)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    @Tag("DeleteClient")
    @DisplayName("DeleteClient")
    void RecruiterService_DeleteClient_ReturnsExceptionRecruiterRepo(){
        String authorization = "Bearer token";
        String recruiterEmail = "admin@example.com";
        String recruiterToDeleteEmail="toDelete@example.com";
        Long recruiterCompanyId = 1L;
        Long clientId = 123L;

        Recruiter currentRecruiter = new Recruiter();
        currentRecruiter.setEmail(recruiterEmail);
        currentRecruiter.setAdmin(true);
        Company company = new Company();
        company.setId(recruiterCompanyId);
        currentRecruiter.setCompany(company);

        Recruiter recruiterToDelete = new Recruiter();
        recruiterToDelete.setEmail(recruiterToDeleteEmail);
        recruiterToDelete.setId(clientId);
        Company deleteCompany = new Company();
        deleteCompany.setId(recruiterCompanyId); // Same company as the current recruiter
        recruiterToDelete.setCompany(deleteCompany);

        when(jwtHelper.extractEmail(authorization)).thenReturn(recruiterEmail);
        when(recruiterRepository.findByEmail(recruiterEmail)).thenReturn(Optional.of(currentRecruiter));
        when(recruiterRepository.findById(clientId)).thenReturn(Optional.of(recruiterToDelete));
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("123456");
        when(iamService.getUserByUsername(anyString(), anyString(), anyString())).thenReturn(List.of(userRepresentation));
        // we used do throw because recruiterrepo.delete is a void method and the when method doesn't accept void methods
        doThrow(new ServerException("Error in Recruiter Database")).when(recruiterRepository).deleteById(anyLong());        
        // Act & Assert
        assertThrows(ServerException.class, () -> 
            recruiterService.deleteClient(123L, authorization));
        
        verify(recruiterRepository, times(1)).deleteById(anyLong());
        verify(iamService, times(1)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    @Tag("sendInvitation")
    @DisplayName("SendInvitation Positive Case")
    void RecruiterService_sendInvitation_ReturnsSuccess(){
        // Arrange
        InvitationEmailRequestDTO invitationRequest = new InvitationEmailRequestDTO("test@example.com", "123456");
        String authorizationHeader = "Bearer Token";
        Long companyId = 1L;
        Long recruiterId = 1L;
        RecruiterDTO recruiterDTO = new RecruiterDTO(null, null, null, "test@example.com", false, null, companyId, Status.INVITED);
        when(validator.verifyIsAdmin(anyString())).thenReturn(true);
        when(validator.userDoesNotExist(anyString(), anyString())).thenReturn(true);
        when(validator.extractCompanyId(anyString())).thenReturn(companyId);
        when(validator.addNewUserToDatabase(recruiterDTO)).thenReturn(recruiterId);
        doNothing().when(emailService).sendInvitationEmail(anyString(), anyLong());
        when(emailValidator.test("test@example.com")).thenReturn(true);
        // Act
        recruiterService.sendInvitation(authorizationHeader, invitationRequest);
        // Assert
        verify(validator, times(1)).verifyIsAdmin(anyString());
        verify(validator, times(1)).userDoesNotExist(anyString(), anyString());
        verify(validator, times(1)).extractCompanyId(anyString());
        verify(validator, times(1)).addNewUserToDatabase(any(RecruiterDTO.class));
        verify(emailService, times(1)).sendInvitationEmail(anyString(), anyLong());
    }

    @Test
    @Tag("sendInvitation")
    @DisplayName("SendInvitation Negative Case: Email empty")
    void RecruiterService_sendInvitation_ReturnsEmailNull(){
        // Arrange
        InvitationEmailRequestDTO invitationRequest = new InvitationEmailRequestDTO(null, null);
        String authorizationHeader = "Bearer Token";
        when(validator.verifyIsAdmin(anyString())).thenReturn(true);
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> 
            recruiterService.sendInvitation(authorizationHeader, invitationRequest));

        verify(validator, times(1)).verifyIsAdmin(anyString());
        verify(validator, times(0)).userDoesNotExist(anyString(), anyString());
        verify(validator, times(0)).extractCompanyId(anyString());
        verify(validator, times(0)).addNewUserToDatabase(any(RecruiterDTO.class));
        verify(emailService, times(0)).sendInvitationEmail(anyString(), anyLong());
    }

    @Test
    @Tag("getClientPersonalInfo")
    @DisplayName("getClientPersonalInfo Positif case")
    void RecruiterService_getClientPersonalInfo_ReturnsRecruiterDTO(){
        // Arrange
        when(authentication.getName()).thenReturn("recruiter1@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        
        // Act
        ClientAccountInfoDTO clientAccountInfoDTO = recruiterService.getClientPersonalInfo();
        // Assert
        assertEquals(clientAccountInfoDTO.getFirstName(), recruiter1.getFirstName());
        assertEquals(clientAccountInfoDTO.getLastName(), recruiter1.getLastName());
        assertEquals(clientAccountInfoDTO.getEmail(), recruiter1.getEmail());
        verify(recruiterRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @Tag("getClientPersonalInfo")
    @DisplayName("getClientPersonalInfo Negative case: Recruiter is empty")
    void RecruiterService_getClientPersonalInfo_ReturnsRecruiterEmpty(){
        // Arrange
        when(authentication.getName()).thenReturn("recruiter99@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(recruiterRepository.findByEmail("recruiter99@gmail.com")).thenReturn(Optional.empty());
        // Act
        // Assert
        assertThrows(InvalidInputException.class,() -> 
            recruiterService.getClientPersonalInfo()
        );
        verify(recruiterRepository, times(1)).findByEmail(anyString());
    }

}

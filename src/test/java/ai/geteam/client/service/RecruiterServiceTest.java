package ai.geteam.client.service;


import ai.geteam.client.dto.ClientAccountInfoDTO;
import ai.geteam.client.dto.InvitationEmailRequestDTO;
import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.dto.RoleDTO;
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
import ai.geteam.client.service.token.TokenServiceImpl;
import ai.geteam.client.utils.MainUtils;
import io.swagger.v3.oas.models.servers.Server;
import ai.geteam.client.entity.recruiter.Status;
import ai.geteam.client.exception.BaseException;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.ServerException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.feign.IamService;
import ai.geteam.client.helper.JwtHelper;
import ai.geteam.client.mapper.ClientAccountInfoMapper;
import ai.geteam.client.mapper.RecruiterMapper;

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
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private TokenServiceImpl tokenService;

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private MainUtils mainUtils;

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
        recruiter1 = new Recruiter(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", true, "0606060606", company1, Status.ACTIVE);
        recruiter2 = new Recruiter(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", company1, Status.BLOCKED);
        recruiter3 = new Recruiter(3L, "recruiterFI3", "recruiterLA3", "recruiter3@gmail.com", false, "0606060606", company2, Status.ACTIVE);
        recruiterDTO1 = new RecruiterDTO(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", true, "0606060606", 1L, Status.ACTIVE);
        recruiterDTO2 = new RecruiterDTO(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", 1L, Status.BLOCKED);
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
    @DisplayName("SendInvitation Negative Case: Email null")
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
    @Tag("sendInvitation")
    @DisplayName("SendInvitation Negative Case: Email empty")
    void RecruiterService_sendInvitation_ReturnsEmailEmpty(){
        // Arrange
        InvitationEmailRequestDTO invitationRequest = new InvitationEmailRequestDTO("", null);
        String authorizationHeader = "Bearer Token";
        when(validator.verifyIsAdmin(anyString())).thenReturn(true);
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> 
            recruiterService.sendInvitation(authorizationHeader, invitationRequest));

        verify(validator, times(1)).verifyIsAdmin(anyString());
        verify(validator, times(0)).userDoesNotExist(anyString(), anyString());
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

    @Test
    @Tag("assignAdminRole")
    @DisplayName("assignAdminRole Positive case")
    void RecruiterService_assignAdminRole_ReturnsAdminRecruiter(){
        // Arrange
        Long userId = 2L;
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setAdmin(true);
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter1@gmail.com");
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter2));
        when(recruiterRepository.save(recruiter2)).thenReturn(recruiter2);
        // Act
        RecruiterDTO result = recruiterService.assignAdminRole(userId, roleDTO);
        // Assert
        assertTrue(result.isAdmin());
        verify(recruiterRepository, times(1)).findById(userId);
    }

    @Test
    @Tag("assignAdminRole")
    @DisplayName("assignAdminRole Negative case: User with userId not found")
    void RecruiterService_assignAdminRole_ReturnsNotFoundUserid(){
        // Arrange
        Long userId = 99L;
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setAdmin(true);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.empty());
        // Act
        assertThrows(InvalidInputException.class, () ->
            recruiterService.assignAdminRole(userId, roleDTO));   
        // Assert
        assertFalse(recruiter2.isAdmin());
        verify(recruiterRepository, times(0)).findByEmail(any());
    }

    @Test
    @Tag("assignAdminRole")
    @DisplayName("assignAdminRole Negative case: Admin not found")
    void RecruiterService_assignAdminRole_ReturnsNotFoundAdmin(){
        // Arrange
        Long userId = 2L;
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setAdmin(true);
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter99@gmail.com");
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter2));
        when(recruiterRepository.findByEmail("recruiter99@gmail.com")).thenReturn(Optional.empty());
        // Act
        assertThrows(InvalidInputException.class, () ->
            recruiterService.assignAdminRole(userId, roleDTO));   
        // Assert
        assertFalse(recruiter2.isAdmin());
        verify(recruiterRepository, times(1)).findByEmail(any());
    }
    
    @Test
    @Tag("assignAdminRole")
    @DisplayName("assignAdminRole Negative case: Recruiter and admin not in the same company")
    void RecruiterService_assignAdminRole_ReturnsNotSameCompany(){
        // Arrange
        Long userId = 2L;
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setAdmin(true);
        recruiter2.setCompany(company2);
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter1@gmail.com");
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter2));
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        // Act
        assertThrows(InvalidInputException.class, () ->
            recruiterService.assignAdminRole(userId, roleDTO));   
        // Assert
        assertFalse(recruiter2.isAdmin());
        verify(recruiterRepository, times(1)).findById(userId);
    }

    @Test
    @Tag("assignAdminRole")
    @DisplayName("assignAdminRole Negative case: Recruiter is not an admin")
    void RecruiterService_assignAdminRole_ReturnsRecruiterNotAdmin(){
        // Arrange
        Long userId = 2L;
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setAdmin(true);
        recruiter1.setAdmin(false);
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter1@gmail.com");
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter2));
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        // Act
        assertThrows(InvalidInputException.class, () ->
            recruiterService.assignAdminRole(userId, roleDTO));   
        // Assert
        assertFalse(recruiter2.isAdmin());
        verify(recruiterRepository, times(1)).findById(userId);
    }

    @Test
    @Tag("updateRecruiter")
    @DisplayName("updateRecruiter Positive case")
    void RecruiterService_updateRecruiter_ReturnsRecruiterUpdated(){
        // Arrange
        String firstName = "updatedFI";
        String lastName = "updatedLA";
        String phone = "+0707070707";
        Recruiter recruiterUpdated = new Recruiter(1L, firstName, lastName, "recruiter99@gmail.com", true, phone, company1, Status.ACTIVE);

        when(mainUtils.getPrincipalMail()).thenReturn("recruiter1@gmail.com");
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByPhone(phone)).thenReturn(Optional.empty());
        when(recruiterRepository.save(recruiter1)).thenReturn(recruiterUpdated);
        // Act
        recruiterService.updateRecruiter(firstName, lastName, phone);
        // Assert
        assertEquals(recruiterUpdated.getFirstName(), firstName); 
        assertEquals(recruiterUpdated.getLastName(), lastName);
        assertEquals(recruiterUpdated.getPhone(), phone); 
        verify(recruiterRepository,times(1)).save(recruiter1);
    }

    @Test
    @Tag("updateRecruiter")
    @DisplayName("updateRecruiter Negative case: Recruiter is null")
    void RecruiterService_updateRecruiter_ReturnsNoRecruiter(){
        // Arrange
        String firstName = "updatedFI";
        String lastName = "updatedLA";
        String phone = "+0707070707";
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter1@gmail.com");
        when(recruiterRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        // Act // Assert
        assertThrows(BaseException.class, () -> 
            recruiterService.updateRecruiter(firstName, lastName, phone));
        
        verify(recruiterRepository,times(0)).save(recruiter1);
    }

    @Test
    @Tag("updateRecruiter")
    @DisplayName("updateRecruiter Negative case: Phone already exists")
    void RecruiterService_updateRecruiter_ReturnsPhoneExists(){
        // Arrange
        String firstName = "updatedFI";
        String lastName = "updatedLA";
        String phone = "0606060606";
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter2@gmail.com");
        when(recruiterRepository.findByEmail("recruiter2@gmail.com")).thenReturn(Optional.of(recruiter2));
        when(recruiterRepository.findByPhone(phone)).thenReturn(Optional.of(recruiter1));
        // Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.updateRecruiter(firstName, lastName, phone));
        // Assert
        verify(recruiterRepository,times(0)).save(recruiter1);
    }

    @Test
    @Tag("updateRecruiter")
    @DisplayName("updateRecruiter Negative case: Invalid phone")
    void RecruiterService_updateRecruiter_ReturnsInvalidPhone(){
        // Arrange
        String firstName = "updatedFI";
        String lastName = "updatedLA";
        String phone = "0707070707";
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter1@gmail.com");
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByPhone(phone)).thenReturn(Optional.empty());
        // Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.updateRecruiter(firstName, lastName, phone));
        // Assert
        verify(recruiterRepository,times(0)).save(recruiter1);
    }

    @Test
    @Tag("updateRecruiter")
    @DisplayName("updateRecruiter Negative case: Invalid firstname")
    void RecruiterService_updateRecruiter_ReturnsInvalidFirstname(){
        // Arrange
        String firstName = "";
        String lastName = "updatedLA";
        String phone = "+0707070707";
        Recruiter recruiterUpdated = new Recruiter(99L, firstName, lastName, "recruiter99@gmail.com", true, phone, company1, Status.ACTIVE);
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter1@gmail.com");
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByPhone(phone)).thenReturn(Optional.empty());
        // Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.updateRecruiter(firstName, lastName, phone));
        // Assert
        verify(recruiterRepository,times(0)).save(recruiter1);
    }

    @Test
    @Tag("updateRecruiter")
    @DisplayName("updateRecruiter Negative case: Invalid lastname")
    void RecruiterService_updateRecruiter_ReturnsInvalidLastname(){
        // Arrange
        String firstName = "updatedfi";
        String lastName = "";
        String phone = "+0707070707";
        Recruiter recruiterUpdated = new Recruiter(99L, firstName, lastName, "recruiter99@gmail.com", true, phone, company1, Status.ACTIVE);
        when(mainUtils.getPrincipalMail()).thenReturn("recruiter1@gmail.com");
        when(recruiterRepository.findByEmail("recruiter1@gmail.com")).thenReturn(Optional.of(recruiter1));
        when(recruiterRepository.findByPhone(phone)).thenReturn(Optional.empty());
        // Act
        assertThrows(InvalidInputException.class, ()->
            recruiterService.updateRecruiter(firstName, lastName, phone));
        // Assert
        verify(recruiterRepository,times(0)).save(recruiter1);
    }

    @Test
    @Tag("activateInvitedClient")
    @DisplayName("activateInvitedClient Positive case")
    void RecruiterService_activateInvitedClient_ReturnsRecruiter(){
        // Arrange
        Long userId = 2L;
        String status = "ACTIVE";
        Recruiter recruiterUpdated = new Recruiter(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", company1, Status.ACTIVE);
        RecruiterDTO recruiterupdatedDTO = RecruiterMapper.toRecruiterDTO(recruiterUpdated);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter2));
        when(recruiterRepository.save(recruiter2)).thenReturn(recruiterUpdated);
        // Act
        RecruiterDTO recruiter = recruiterService.activateInvitedClient(userId, status);

        // Assert
        assertEquals(recruiterupdatedDTO, recruiter);
        verify(recruiterRepository,times(1)).save(recruiter2);
    }

    @Test
    @Tag("activateInvitedClient")
    @DisplayName("activateInvitedClient Negative case: Recruiter is not found")
    void RecruiterService_activateInvitedClient_ReturnsNoRecruiter(){
        // Arrange
        Long userId = 99L;
        String status = "ACTIVE";
        when(recruiterRepository.findById(userId)).thenReturn(Optional.empty());
        // Act
        assertThrows(InvalidInputException.class, () ->
                    recruiterService.activateInvitedClient(userId, status));
        // Assert
        verify(recruiterRepository,times(0)).save(recruiter2);
    }

    @Test
    @Tag("getUserEmail")
    @DisplayName("getUserEmail Positive case")
    void RecruiterService_getUserEmail_ReturnsEmail(){
        // Arrange
        when(authentication.getName()).thenReturn("recruiter99@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Act
        String email = recruiterService.getUserEmail();
        // Assert
        assertEquals("recruiter99@gmail.com", email);
    }

    // @Test
    // @Tag("unblockRecruiterInKeycloak")
    // @DisplayName("unblockRecruiterInKeycloak Positive case")
    // void RecruiterService_unblockRecruiterInKeycloak_ReturnsUnblocked(){
    //     // Arrange
    //     String email = "recruiter2@gmail.com";
    //     String authorization = "Bearer Token";
    //     String realm = "client";
    //     ResponseEntity<String> response = ResponseEntity.ok().build();
    //     when(iamService.unblockRecruiter(realm, email, authorization)).thenReturn(response);
    //     // Act
    //     recruiterService.unblockRecruiterInKeycloak(email, authorization);
    // }


    @Test
    @Tag("updateRecruiterStatus")
    @DisplayName("updateRecruiterStatus Positive case")
    void RecruiterService_updateRecruiterStatus_ReturnsRecruiter(){
        // Arrange
        Long userId = 2L;
        String authorization = "Bearer Token";
        String email = "recruiter2@gmail.com";
        Recruiter recruiterUpdated = new Recruiter(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", company1, Status.ACTIVE);
        RecruiterDTO recruiterupdatedDTO = RecruiterMapper.toRecruiterDTO(recruiterUpdated);
        doNothing().when(validator).validateAdmin(authorization);
        doNothing().when(validator).validateStatusActive(authorization);
        doNothing().when(validator).validateSameCompany(recruiter2, authorization);
        doNothing().when(validator).validateRecruiterStatus(recruiter2);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter2));
        when(recruiterRepository.save(recruiter2)).thenReturn(recruiterUpdated);
        ResponseEntity<String> response = ResponseEntity.ok().build();
        when(iamService.unblockRecruiter(null, email, authorization)).thenReturn(response);
        RecruiterDTO result = recruiterService.updateRecruiterStatus(userId, authorization);
        // Assert
        assertEquals(recruiterupdatedDTO, result);
        verify(recruiterRepository,times(1)).save(recruiter2);
    }

    @Test
    @Tag("updateRecruiterStatus")
    @DisplayName("updateRecruiterStatus Negative case: Recruiter not found")
    void RecruiterService_updateRecruiterStatus_ReturnsNoRecruiter(){
        // Arrange
        Long userId = 99L;
        String authorization = "Bearer Token";when(recruiterRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(InvalidInputException.class, ()->
            recruiterService.updateRecruiterStatus(userId, authorization));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }

    @Test
    @Tag("updateRecruiterStatus")
    @DisplayName("updateRecruiterStatus Negative case: unable to unblock client")
    void RecruiterService_updateRecruiterStatus_ReturnsErrorUnblock(){
        // Arrange
        Long userId = 2L;
        String authorization = "Bearer Token";
        String email = "recruiter2@gmail.com";
        doNothing().when(validator).validateAdmin(authorization);
        doNothing().when(validator).validateStatusActive(authorization);
        doNothing().when(validator).validateSameCompany(recruiter2, authorization);
        doNothing().when(validator).validateRecruiterStatus(recruiter2);
        when(recruiterRepository.findById(userId)).thenReturn(Optional.of(recruiter2));
        ResponseEntity<String> response = ResponseEntity.badRequest().build();
        when(iamService.unblockRecruiter(null, email, authorization)).thenReturn(response);
        assertThrows(BaseException.class, ()->
                    recruiterService.updateRecruiterStatus(userId, authorization));
        // Assert
        verify(recruiterRepository,times(0)).save(any());
    }


}


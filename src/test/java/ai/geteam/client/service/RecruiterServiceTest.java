package ai.geteam.client.service;


import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.repository.RecruiterRepository;
import ai.geteam.client.service.recruiter.RecruiterService;
import ai.geteam.client.service.recruiter.RecruiterServiceImpl;
import ai.geteam.client.entity.recruiter.Status;
import ai.geteam.client.exception.InvalidInputException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
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

    @BeforeEach
    public void setUp() {
        company1 = new Company(1L, "Company Test 1", "www.companytest1.com", "size1", null, null, null, null, null);
        company2 = new Company(2L, "Company Test 2", "www.companytest2.com", "size2", null, null, null, null, null);
        recruiter1 = new Recruiter(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", false, "0606060606", company1, Status.ACTIVE);
        recruiter2 = new Recruiter(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", company1, Status.ACTIVE);
        recruiter3 = new Recruiter(3L, "recruiterFI3", "recruiterLA3", "recruiter3@gmail.com", false, "0606060606", company2, Status.ACTIVE);
        recruiterDTO1 = new RecruiterDTO(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", false, "0606060606", 1L, Status.ACTIVE);
        recruiterDTO2 = new RecruiterDTO(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0707070707", 1L, Status.ACTIVE);
        recruiterDTO3 = new RecruiterDTO(3L, "recruiterFI3", "recruiterLA3", "recruiter3@gmail.com", false, "0606060606", 2L, Status.ACTIVE);
        
        // MockitoAnnotations.openMocks(this);
        
    }

    @Test
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
}

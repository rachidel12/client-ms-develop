package ai.geteam.client.service;


import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.repository.RecruiterRepository;
import ai.geteam.client.service.recruiter.RecruiterService;
import ai.geteam.client.service.recruiter.RecruiterServiceImpl;
import ai.geteam.client.entity.recruiter.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private RecruiterRepository recruiterRepository;

    @InjectMocks
    private RecruiterServiceImpl recruiterService;

    private Recruiter recruiter1;
    private Recruiter recruiter2;
    private RecruiterDTO recruiterDTO1;
    private RecruiterDTO recruiterDTO2;
    private Company company1;
    private Company company2;

    @BeforeEach
    public void setUp() {
        company1 = new Company(1L, "Company Test 1", "www.companytest1.com", "size1", null, null, null, null, null);
        company2 = new Company(2L, "Company Test 2", "www.companytest2.com", "size2", null, null, null, null, null);
        recruiter1 = new Recruiter(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", false, "0606060606", company1, Status.ACTIVE);
        recruiter2 = new Recruiter(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0606060606", company2, Status.ACTIVE);
        recruiterDTO1 = new RecruiterDTO(1L, "recruiterFI1", "recruiterLA1", "recruiter1@gmail.com", false, "0606060606", 1L, Status.ACTIVE);
        recruiterDTO2 = new RecruiterDTO(2L, "recruiterFI2", "recruiterLA2", "recruiter2@gmail.com", false, "0606060606", 2L, Status.ACTIVE);
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
}

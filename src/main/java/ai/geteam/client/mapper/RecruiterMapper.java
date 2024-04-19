package ai.geteam.client.mapper;

import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.entity.recruiter.Recruiter;

public class RecruiterMapper {

    private RecruiterMapper() {
    }

    public static RecruiterDTO toRecruiterDTO(Recruiter recruiter) {
        return RecruiterDTO.builder()
                .id(recruiter.getId())
                .firstName(recruiter.getFirstName())
                .lastName(recruiter.getLastName())
                .email(recruiter.getEmail())
                .admin(recruiter.isAdmin())
                .phone(recruiter.getPhone())
                .companyId(recruiter.getCompany().getId())
                .status(recruiter.getStatus())
                .phone(recruiter.getPhone())
                .build();
    }

    public static Recruiter toRecruiter(RecruiterDTO recruiterDTO) {
        return Recruiter.builder()
                .id(recruiterDTO.getId())
                .firstName(recruiterDTO.getFirstName())
                .lastName(recruiterDTO.getLastName())
                .email(recruiterDTO.getEmail())
                .admin(recruiterDTO.isAdmin())
                .phone(recruiterDTO.getPhone())
                .status(recruiterDTO.getStatus())
                .build();
    }
}

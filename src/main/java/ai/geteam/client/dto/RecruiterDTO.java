package ai.geteam.client.dto;

import ai.geteam.client.entity.recruiter.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private boolean admin;

    private String phone;

    private Long companyId;

    private Status status;

}

package ai.geteam.client.dto;

import ai.geteam.client.entity.recruiter.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientAccountInfoDTO {
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private boolean admin;

    private String phone;

    private CompanyDTO company;

    private Status status;

    private AccountTypeDTO accountTypeDTO;
}

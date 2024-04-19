package ai.geteam.client.mapper;

import ai.geteam.client.dto.AccountTypeDTO;
import ai.geteam.client.dto.ClientAccountInfoDTO;
import ai.geteam.client.entity.recruiter.Recruiter;

public class ClientAccountInfoMapper {

    private ClientAccountInfoMapper() {
        //this private constructor to fix sonar issue
    }

    public static ClientAccountInfoDTO toClientAccountInfo(Recruiter recruiter){
        return ClientAccountInfoDTO.builder()
                .company(CompanyMapper.toCompanyDTO(recruiter.getCompany()))
                .id(recruiter.getId())
                .firstName(recruiter.getFirstName())
                .lastName(recruiter.getLastName())
                .email(recruiter.getEmail())
                .admin(recruiter.isAdmin())
                .status(recruiter.getStatus())
                .accountTypeDTO(AccountTypeDTO.builder()
                        .id(2l)
                        .name("Client")
                        .build())
                .build();
    }
}

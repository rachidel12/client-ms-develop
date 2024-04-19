package ai.geteam.client.service.company;

import ai.geteam.client.dto.CompanyCountryInfoDTO;
import ai.geteam.client.dto.CompanyDTO;


public interface CompanyService {

   public CompanyDTO updateCompanyById(CompanyDTO companyDTO, String token);
    String create(CompanyDTO companyDTO);

    String delete(Long id);

    CompanyCountryInfoDTO getCompanyInfo(String token);
}

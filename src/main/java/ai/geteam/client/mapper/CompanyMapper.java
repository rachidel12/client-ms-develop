package ai.geteam.client.mapper;

import ai.geteam.client.dto.CompanyDTO;
import ai.geteam.client.dto.HiringConsultantDTO;
import ai.geteam.client.entity.Company;

import java.util.ArrayList;

public class CompanyMapper {

    private CompanyMapper() {
    }

    public static Company toCompany(CompanyDTO companyDTO) {
        return Company.builder()
                .id(companyDTO.getId())
                .name(companyDTO.getName())
                .website(companyDTO.getWebsite())
                .hiringConsultantName(companyDTO.getHiringConsultantName())
                .hiringConsultantEmail(companyDTO.getHiringConsultantEmail())
                .size(companyDTO.getSize())
                .hiringConsultantEmail(companyDTO.getHiringConsultantEmail())
                .hiringConsultantName(companyDTO.getHiringConsultantName())
                .recruiters(new ArrayList<>())
                .build();
    }

    public static CompanyDTO toCompanyDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .website(company.getWebsite())
                .size(company.getSize())
                .countryId(company.getCountry().getId())
                .cityId(company.getCity().getId())
                .hiringConsultantEmail(company.getHiringConsultantEmail())
                .hiringConsultantName(company.getHiringConsultantName())
                .stateId(company.getState() != null ? company.getState().getId() : null)
                .build();
    }

    public static HiringConsultantDTO toHiringConsultantDto(Company company) {
        return HiringConsultantDTO.builder()
                .name(company.getHiringConsultantName())
                .email(company.getHiringConsultantEmail())
                .build();
    }
}

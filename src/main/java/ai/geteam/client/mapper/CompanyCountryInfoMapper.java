package ai.geteam.client.mapper;

import ai.geteam.client.dto.CompanyCountryInfoDTO;
import ai.geteam.client.dto.location.CountryDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.location.Country;

public class CompanyCountryInfoMapper {

    private CompanyCountryInfoMapper() {
        //
    }

    public static CompanyCountryInfoDTO toDto(Company company) {
        return CompanyCountryInfoDTO.builder()
                .name(company.getName())
                .website(company.getWebsite())
                .size(company.getSize())
                .country(toCountryDTO(company.getCountry()))
                .build();
    }

    public static CountryDTO toCountryDTO(Country country) {
        if (country == null) {
            return null;
        }

        return CountryDTO.builder()
                .id(country.getId())
                .name(country.getName())
                .code(country.getCode())
                .build();
    }
}
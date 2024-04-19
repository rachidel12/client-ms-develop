package ai.geteam.client.mapper.location;

import ai.geteam.client.dto.location.CountryDTO;
import ai.geteam.client.entity.location.Country;

public class CountryMapper {

    private CountryMapper() {

    }

    public static CountryDTO toCountryDTO(Country country) {
        return CountryDTO.builder()
                .id(country.getId())
                .name(country.getName())
                .code(country.getCode())
                .build();
    }

    public static Country toCountry(CountryDTO countryDTO) {
        return Country.builder()
                .id(countryDTO.getId())
                .name(countryDTO.getName())
                .build();
    }
}

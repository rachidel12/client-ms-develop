package ai.geteam.client.mapper.location;

import ai.geteam.client.dto.location.CityDTO;
import ai.geteam.client.entity.location.City;

public class CityMapper {

    private CityMapper() {}

    public static CityDTO toCityDTO(City city) {
        return CityDTO.builder()
                .id(city.getId())
                .name(city.getName())
                .code(city.getCode())
                .countryId(city.getCountry().getId())
                .build();
    }

    public static City toCity(CityDTO cityDTO) {
        return City.builder()
                .id(cityDTO.getId())
                .name(cityDTO.getName())
                .code(cityDTO.getCode())
                .build();
    }

}

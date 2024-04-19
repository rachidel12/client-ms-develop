package ai.geteam.client.mapper.location;

import ai.geteam.client.dto.location.ContinentDTO;
import ai.geteam.client.entity.location.Continent;

public class ContinentMapper {
    private ContinentMapper() {
    }

    public static ContinentDTO toContinentDTO(Continent continent) {
        return ContinentDTO.builder()
                .id(continent.getId())
                .name(continent.getName())
                .code(continent.getCode())
                .build();
    }

    public static Continent toContinent(ContinentDTO continentDTO) {
        return Continent.builder()
                .id(continentDTO.getId())
                .name(continentDTO.getName())
                .code(continentDTO.getCode())
                .build();
    }
}

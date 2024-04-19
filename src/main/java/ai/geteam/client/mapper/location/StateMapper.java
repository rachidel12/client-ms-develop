package ai.geteam.client.mapper.location;

import ai.geteam.client.dto.location.StateDTO;
import ai.geteam.client.entity.location.Country;
import ai.geteam.client.entity.location.State;

public class StateMapper {

    private StateMapper() {

    }

    public static StateDTO toStateDTO(State state) {
        return StateDTO.builder()
                .id(state.getId())
                .name(state.getName())
                .code(state.getCode())
                .countryId(state.getCountry().getId())
                .build();
    }

    public static State toState(StateDTO stateDTO) {
        return State.builder()
                .id(stateDTO.getId())
                .name(stateDTO.getName())
                .code(stateDTO.getCode())
                .country(Country.builder().id(stateDTO.getCountryId()).build())
                .build();
    }
}

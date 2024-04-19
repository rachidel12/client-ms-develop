package ai.geteam.client.dto;

import ai.geteam.client.dto.location.CountryDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyCountryInfoDTO {
    private String name;
    private String website;
    private String size;
    private CountryDTO country;
}

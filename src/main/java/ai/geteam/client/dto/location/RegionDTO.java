package ai.geteam.client.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDTO {

    private Long id;

    private String name;

    private String code;

    private Long continentId;
}

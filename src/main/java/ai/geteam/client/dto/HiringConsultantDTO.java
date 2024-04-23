package ai.geteam.client.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HiringConsultantDTO {
    private String name;

    private String email;
}

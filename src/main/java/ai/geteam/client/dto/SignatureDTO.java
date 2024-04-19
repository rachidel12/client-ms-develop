package ai.geteam.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureDTO {

    private Long id;

    private String name;

    private String value;

    private boolean defaultSign;

}

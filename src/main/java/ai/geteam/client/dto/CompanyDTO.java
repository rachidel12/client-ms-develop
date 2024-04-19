package ai.geteam.client.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {

    private Long id;

    private String name;

    private String website;

    private String size;

    private Long countryId;

    private Long stateId;

    private Long cityId;

    private RecruiterDTO recruiter;

}

package ai.geteam.client.entity.signatue;

import ai.geteam.client.entity.Company;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="signature")
public class Signature {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Name name;
    @Lob
    @Column(unique = true, nullable = false,columnDefinition = "TEXT")
    private String value;
    @Column(nullable = false)
    boolean defaultValue;
    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Company company;
}

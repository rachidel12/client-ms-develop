package ai.geteam.client.entity;


import ai.geteam.client.entity.location.City;
import ai.geteam.client.entity.location.Country;
import ai.geteam.client.entity.location.State;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.entity.signatue.Signature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

@DynamicInsert
@DynamicUpdate

@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String website;

    @Column(nullable = false)
    private String size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recruiter> recruiters;

    @OneToMany(mappedBy = "company")
    private List<Signature> signatures;

    public void addRecruiter(Recruiter recruiter) {
        this.recruiters.add(recruiter);
        recruiter.setCompany(this);
    }

    public void removeRecruiter(Recruiter recruiter) {
        this.recruiters.remove(recruiter);
        recruiter   .setCompany(null);
    }

    public List<Signature> addSignature(Signature signature){
        this.signatures.add(signature);
        return this.signatures;
    }
}

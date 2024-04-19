package ai.geteam.client.repository;


import ai.geteam.client.entity.signatue.Signature;
import org.springframework.data.jpa.repository.JpaRepository;
import ai.geteam.client.entity.Company;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignatureRepository extends JpaRepository<Signature,Long> {

    List<Signature> findAllByCompany(Company company);
    
    List<Signature> findByCompanyIdAndDefaultValueTrue(Long companyId);
}

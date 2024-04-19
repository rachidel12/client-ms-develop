package ai.geteam.client.repository;

import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.recruiter.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {

    boolean existsByEmail(String email);

    Optional<Recruiter> findByEmail(String email);
    Optional<Recruiter> findByPhone(String phone);

    List<Recruiter> findAllByCompany(Company company);
}

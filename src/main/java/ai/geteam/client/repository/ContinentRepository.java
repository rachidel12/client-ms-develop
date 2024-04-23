package ai.geteam.client.repository;

import ai.geteam.client.entity.location.Continent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContinentRepository extends JpaRepository<Continent,Long> {
}

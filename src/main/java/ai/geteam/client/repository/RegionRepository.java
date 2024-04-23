package ai.geteam.client.repository;

import ai.geteam.client.entity.location.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region,Long> {
}

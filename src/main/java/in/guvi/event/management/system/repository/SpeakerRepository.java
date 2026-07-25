package in.guvi.event.management.system.repository;

import in.guvi.event.management.system.entity.Speaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpeakerRepository extends JpaRepository<Speaker, Long> {

    List<Speaker> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}

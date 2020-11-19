package imooc.naga.repository.system;

import imooc.naga.entity.system.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
 Team findOneByName(String name);
}

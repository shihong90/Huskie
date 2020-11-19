package imooc.naga.repository.system;

import imooc.naga.entity.system.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
  User findOneByName(String name);

}

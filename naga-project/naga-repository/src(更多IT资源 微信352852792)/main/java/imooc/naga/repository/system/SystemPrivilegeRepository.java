package imooc.naga.repository.system;

import imooc.naga.entity.system.PrivilegeType;
import imooc.naga.entity.system.SystemPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemPrivilegeRepository extends JpaRepository<SystemPrivilege, Long> {
  SystemPrivilege findOneByTeamAndPrivilegeType(String team, PrivilegeType privilegeType);

}

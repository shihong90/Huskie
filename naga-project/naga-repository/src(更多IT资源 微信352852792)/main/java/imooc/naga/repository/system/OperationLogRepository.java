package imooc.naga.repository.system;

import imooc.naga.entity.system.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long>,
    JpaSpecificationExecutor<OperationLog> {
  Page<OperationLog> findByUser(String user, Pageable pageable);

  Page<OperationLog> findByObjLike(String objLike, Pageable pageable);

  Page<OperationLog> findByUserAndObjLike(String user, String objLike, Pageable pageable);

}

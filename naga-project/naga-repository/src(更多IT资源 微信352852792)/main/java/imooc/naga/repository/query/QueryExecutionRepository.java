package imooc.naga.repository.query;

import imooc.naga.entity.query.QueryExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryExecutionRepository extends JpaRepository<QueryExecution, Long> {
}

package imooc.naga.repository.query;

import imooc.naga.entity.query.SavedSql;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedSqlRepository extends JpaRepository<SavedSql, Long> {
    List<SavedSql> findByCreator(String creator);
}

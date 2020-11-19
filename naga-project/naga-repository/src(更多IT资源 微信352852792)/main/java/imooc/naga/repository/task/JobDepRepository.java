package imooc.naga.repository.task;

import imooc.naga.entity.task.JobDep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobDepRepository extends JpaRepository<JobDep, Long> {

  void deleteByTaskId(String taskId);

  List<JobDep> findByTaskId(String taskId);
}

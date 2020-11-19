package imooc.naga.repository.task;

import imooc.naga.entity.task.TaskWorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskWorkFlowRepository extends JpaRepository<TaskWorkFlow, String> {

  TaskWorkFlow findOneByName(String name);
}

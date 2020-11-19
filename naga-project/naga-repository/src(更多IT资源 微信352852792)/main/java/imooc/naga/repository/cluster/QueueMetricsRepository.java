package imooc.naga.repository.cluster;

import imooc.naga.entity.cluster.QueueMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueMetricsRepository extends JpaRepository<QueueMetrics, Long> {
    List<QueueMetrics> findByCreateTime(Integer selectTime);
}

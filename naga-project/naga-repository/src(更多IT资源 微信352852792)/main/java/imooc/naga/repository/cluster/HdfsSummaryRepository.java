package imooc.naga.repository.cluster;

import imooc.naga.entity.cluster.HdfsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HdfsSummaryRepository extends JpaRepository<HdfsSummary, Long> {
    HdfsSummary findTop1ByIsTrashFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(Integer selectTime);

    List<HdfsSummary> findByIsTrashFalseAndCreateTimeBetweenOrderByCreateTimeAsc(Integer startTime,Integer endTime);

}

package imooc.naga.core.client.azkaban.response;

import imooc.naga.core.client.azkaban.model.Schedule;
import lombok.Data;

@Data
public class FetchScheduleResponse extends BaseResponse {
    private Schedule schedule;

}
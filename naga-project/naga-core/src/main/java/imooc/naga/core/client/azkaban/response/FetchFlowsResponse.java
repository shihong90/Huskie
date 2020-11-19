package imooc.naga.core.client.azkaban.response;

import imooc.naga.core.client.azkaban.model.Flow;
import lombok.Data;

import java.util.List;

@Data
public class FetchFlowsResponse extends BaseResponse {
    private String project;
    private String projectId;
    private List<Flow> flows;
}

package imooc.naga.core.client.azkaban.response;

import lombok.Data;

@Data
public class ProjectZipResponse extends BaseResponse {
    private String projectId;
    private String version;
}

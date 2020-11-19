package imooc.naga.server.plugin;

import imooc.naga.entity.plugin.PackageOutParam;
import imooc.naga.entity.plugin.PackageParam;
import lombok.Data;

import java.util.List;

@Data
public class PackageMetaInfo {
    private String name;
    private String version;
    private String jobType;
    private String language;
    private List<PackageParam> pkgParams;
    private List<PackageOutParam> outParams;
}

package imooc.naga.entity.plugin;

import imooc.naga.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "plugin_package")
public class PluginPackage extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String version;
  private String pluginType;
  @Enumerated(EnumType.STRING)
  private PluginCategory pluginCategory;
  private String jobType;
  private String lang;
  private String admin;
  private String team;
  private String projectName;
  private Long projectId;
  private String pluginDesc; //描述
  @Column(columnDefinition = "TEXT")
  @Convert(converter = ParamJpaConverterJson.class)
  private List<PackageParam> defaultParams; //输入参数
  @Column(columnDefinition = "TEXT")
  @Convert(converter = OutParamJpaConverterJson.class)
  private List<PackageOutParam> outParams; //输出参数
  @Enumerated(EnumType.STRING)
  private PluginStatus pluginStatus;
  private String tags;
  private String pkgPath; //插件程序保存路径

}

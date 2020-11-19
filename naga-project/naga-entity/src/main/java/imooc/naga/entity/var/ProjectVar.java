package imooc.naga.entity.var;

import imooc.naga.entity.BaseEntity;
import imooc.naga.entity.JpaConverterJson;
import imooc.naga.entity.plugin.ParamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "project_var")
public class ProjectVar extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  @Enumerated(EnumType.STRING)
  private VariableType varType;
  private Boolean bind;
  private String bindTaskName;
  private String bindJobName;
  private String bindParam;
  private Integer bindTime;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = JpaConverterJson.class)
  private Object initValue;
  private String varDesc;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = VarJpaConverterJson.class)
  private VariableValue currentValue;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = VarJpaConverterJson.class)
  private VariableValue preValue;
  private Integer updateTime;
  private Integer initTime;
  private ParamType paramType;
  private String projectName;
  private Long projectId;
  private String admin;
  private String team;

  @Override
  public String toString(){
    return String.format("ProjectVar %s", name);
  }
}

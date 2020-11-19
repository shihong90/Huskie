package imooc.naga.entity.privilege;

import imooc.naga.entity.BaseEntity;
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
@Table(name = "naga_resource_privilege")
public class ResourcePrivilege extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String team;
  @Enumerated(EnumType.STRING)
  private ResourceType resourceType;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = ResourceJpaConverterJson.class)
  private Resource resource;
}

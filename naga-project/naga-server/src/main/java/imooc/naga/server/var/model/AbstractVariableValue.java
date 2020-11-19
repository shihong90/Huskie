package imooc.naga.server.var.model;

import imooc.naga.entity.var.VariableType;
import imooc.naga.entity.var.VariableValue;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public abstract class AbstractVariableValue implements VariableValue {

  protected VariableType variableType;
}

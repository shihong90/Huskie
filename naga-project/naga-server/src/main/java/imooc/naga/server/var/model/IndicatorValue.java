package imooc.naga.server.var.model;


import imooc.naga.entity.plugin.ParamType;
import imooc.naga.entity.var.VariableType;

public class IndicatorValue extends AbstractVariableValue {

  private Object value;
  private ParamType paramType;

  public IndicatorValue() {
    this.variableType = VariableType.Indicator;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(Object valueString) {
    this.value = valueString;
  }

  @Override
  public ParamType getParamType() {
    return paramType;
  }

  public void setParamType(ParamType paramType) {
    this.paramType = paramType;
  }

  @Override
  public void applyUpdate(Object value) {
    this.value = value;
  }

}

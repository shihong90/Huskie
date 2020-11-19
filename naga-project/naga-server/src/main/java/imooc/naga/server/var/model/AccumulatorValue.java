package imooc.naga.server.var.model;


import imooc.naga.entity.plugin.ParamType;
import imooc.naga.entity.var.VariableType;

public class AccumulatorValue extends AbstractVariableValue {

  private ParamType type;
  private Object value;

  public AccumulatorValue(ParamType paramType) {
    this.type = paramType;
    this.variableType = VariableType.Accumulator;
  }

  public AccumulatorValue() {
    this.variableType = VariableType.Accumulator;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(Object valueString) {
    this.value=valueString;
  }

  @Override
  public ParamType getParamType() {
    return type;
  }

  public void setParamType(ParamType type){
    this.type = type;
  }

  @Override
  public void applyUpdate(Object valueV) {
    Number origValue = (Number) value;
    Number newValue = (Number) valueV;
    if (type.equals(ParamType.FLOAT)) {
      float orgV = origValue.floatValue();
      float newV = newValue.floatValue();
      value = orgV + newV;
    } else if (type.equals(ParamType.INT)) {
      int origV = origValue.intValue();
      int newV = newValue.intValue();
      value = origV + newV;
    }
  }

}

package com.hsiaosiyuan.jexpose.signature.node;

public class TypeArg extends Node {
  public static final TypeArg wildcard = new TypeArg(true);

  public boolean isWildcard;
  public String prefix;
  public FieldTypeSignature type;

  public TypeArg() {
  }

  public TypeArg(boolean isWildcard) {
    this.isWildcard = isWildcard;
  }
}
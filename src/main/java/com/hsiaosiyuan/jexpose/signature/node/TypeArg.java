package com.hsiaosiyuan.jexpose.signature.node;

import java.util.HashSet;

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

  @Override
  public HashSet<String> collectRefClasses() {
    if (type == null) return new HashSet<>();
    return type.collectRefClasses();
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    if (type == null) return new HashSet<>();
    return type.getDirectRefClasses();
  }
}
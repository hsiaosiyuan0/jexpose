package com.hsiaosiyuan.jexpose.signature.node;

import java.util.HashMap;
import java.util.HashSet;

public class TypeVar extends FieldTypeSignature {
  public String name;

  public boolean isTypeVar = true;

  public TypeVar(String name) {
    this.name = name;
  }

  @Override
  public HashSet<String> collectRefClasses() {
    return new HashSet<>();
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    return new HashSet<>();
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    return new TypeVar(name);
  }

  @Override
  void applyTypeArgs(HashMap<String, TypeArg> args) {
  }
}
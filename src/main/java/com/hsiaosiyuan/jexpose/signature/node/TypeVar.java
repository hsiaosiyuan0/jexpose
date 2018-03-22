package com.hsiaosiyuan.jexpose.signature.node;

import java.util.HashSet;

public class TypeVar extends FieldTypeSignature {
  public String name;

  public TypeVar(String name) {
    this.name = name;
  }

  @Override
  public HashSet<String> collectRefClasses() {
    return new HashSet<>();
  }
}
package com.hsiaosiyuan.jexpose.signature.node;

import java.util.HashSet;

public class ArrayTypeSignature extends FieldTypeSignature {
  public boolean isArray = true;
  public TypeSignature elementType;

  @Override
  public HashSet<String> collectRefClasses() {
    return elementType.collectRefClasses();
  }
}
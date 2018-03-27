package com.hsiaosiyuan.jexpose.signature.node;

import java.util.ArrayList;
import java.util.HashSet;

public class TypeParam extends Node {
  public String name;
  public ArrayList<FieldTypeSignature> types;

  public TypeParam() {
    types = new ArrayList<>();
  }

  @Override
  public HashSet<String> collectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (FieldTypeSignature ts : types) {
      refs.addAll(ts.collectRefClasses());
    }
    return refs;
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (FieldTypeSignature ts : types) {
      refs.addAll(ts.getDirectRefClasses());
    }
    return refs;
  }
}

package com.hsiaosiyuan.jexpose.signature.node;

import java.util.ArrayList;
import java.util.HashSet;

public class MethodTypeSignature extends Node {
  public ArrayList<TypeParam> typeParams;
  public ArrayList<TypeSignature> params;
  public TypeSignature ret;
  public ArrayList<Node> exceptions;

  public MethodTypeSignature() {
    typeParams = new ArrayList<>();
    params = new ArrayList<>();
    exceptions = new ArrayList<>();
  }

  public boolean hasParams() {
    return params.size() > 0;
  }

  @Override
  public HashSet<String> collectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (TypeParam tp : typeParams) {
      refs.addAll(tp.collectRefClasses());
    }
    for (TypeSignature ts : params) {
      refs.addAll(ts.collectRefClasses());
    }
    refs.addAll(ret.collectRefClasses());
    return refs;
  }
}

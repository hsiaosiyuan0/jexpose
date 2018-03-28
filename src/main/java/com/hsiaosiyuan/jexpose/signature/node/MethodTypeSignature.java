package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.HashSet;

public class MethodTypeSignature extends Node {
  public ArrayList<TypeParam> typeParams;
  public ArrayList<TypeSignature> params;
  public TypeSignature ret;

  public boolean isOverride;

  @JSONField(serialize = false)
  public ArrayList<Node> exceptions;

  public MethodTypeSignature() {
    typeParams = new ArrayList<>();
    params = new ArrayList<>();
    exceptions = new ArrayList<>();
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

  @Override
  public HashSet<String> getDirectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (TypeParam tp : typeParams) {
      refs.addAll(tp.getDirectRefClasses());
    }
    for (TypeSignature ts : params) {
      refs.addAll(ts.getDirectRefClasses());
    }
    refs.addAll(ret.getDirectRefClasses());
    return refs;
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}

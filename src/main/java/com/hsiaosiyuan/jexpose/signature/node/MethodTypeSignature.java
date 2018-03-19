package com.hsiaosiyuan.jexpose.signature.node;

import java.util.ArrayList;

public class MethodTypeSignature extends Node {
  public ArrayList<TypeParam> typeParams;
  public ArrayList<TypeSignature> params;
  public TypeSignature ret;
  public ArrayList<Node> exceptions;

  public boolean hasParams() {
    return params != null && params.size() > 0;
  }
}

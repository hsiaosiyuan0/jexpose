package com.hsiaosiyuan.jexpose.signature.node;

import java.util.HashSet;

public class Primitive extends TypeSignature {
  public String binaryName;

  public Primitive(String binaryName) {
    this.binaryName = binaryName;
  }

  @Override
  public HashSet<String> collectRefClasses() {
    return new HashSet<>() {{
      add(binaryName);
    }};
  }
}
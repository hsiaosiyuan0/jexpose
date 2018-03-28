package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashSet;

public class Primitive extends TypeSignature {
  @JSONField(serialize = false)
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

  @Override
  public HashSet<String> getDirectRefClasses() {
    return new HashSet<>();
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    return new Primitive(binaryName);
  }

  @JSONField(name = "name")
  public String getName() {
    return binaryName.replace("/", ".");
  }
}
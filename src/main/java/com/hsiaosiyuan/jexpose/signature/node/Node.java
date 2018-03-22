package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashSet;

public abstract class Node {

  @JSONField(serialize=false)
  public boolean isPrimitive() {
    return this instanceof Primitive;
  }

  @JSONField(serialize=false)
  public boolean isArray() {
    return this instanceof ArrayTypeSignature;
  }

  @JSONField(serialize=false)
  public boolean isClassType() {
    return this instanceof ClassTypeSignature;
  }

  @JSONField(serialize=false)
  public boolean isClass() {
    return this instanceof ClassSignature;
  }

  @JSONField(serialize=false)
  public boolean isTypeVar() {
    return this instanceof TypeVar;
  }

  @JSONField(serialize=false)
  public Primitive asPrimitive() {
    return (Primitive) this;
  }

  @JSONField(serialize=false)
  public ArrayTypeSignature asArray() {
    return (ArrayTypeSignature) this;
  }

  @JSONField(serialize=false)
  public ClassTypeSignature asClassType() {
    return (ClassTypeSignature) this;
  }

  @JSONField(serialize=false)
  public ClassSignature asClass() {
    return (ClassSignature) this;
  }

  @JSONField(serialize=false)
  public TypeVar asTypeVar() {
    return (TypeVar) this;
  }

  @JSONField(serialize=false)
  public abstract HashSet<String> collectRefClasses();
}
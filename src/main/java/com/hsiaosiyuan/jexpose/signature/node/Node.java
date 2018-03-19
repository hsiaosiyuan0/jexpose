package com.hsiaosiyuan.jexpose.signature.node;

public class Node {

  public boolean isPrimitive() {
    return this instanceof Primitive;
  }

  public boolean isArray() {
    return this instanceof ArrayTypeSignature;
  }

  public boolean isClass() {
    return this instanceof ClassTypeSignature;
  }

  public boolean isTypeVar() {
    return this instanceof TypeVar;
  }

  public boolean isVoidType() {
    return this instanceof VoidType;
  }

  public Primitive asPrimitive() {
    return (Primitive) this;
  }

  public ArrayTypeSignature asArray() {
    return (ArrayTypeSignature) this;
  }

  public ClassTypeSignature asClass() {
    return (ClassTypeSignature) this;
  }

  public TypeVar asTypeVar() {
    return (TypeVar) this;
  }
}
package com.hsiaosiyuan.jexpose.signature.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ClassSignature extends Node {
  public String binaryName;
  public ArrayList<TypeParam> typeParams;
  public ArrayList<ClassTypeSignature> superClasses;

  public boolean isInterface;
  public HashMap<String, TypeSignature> fields;
  public HashMap<String, MethodTypeSignature> methods;

  public ClassSignature() {
    typeParams = new ArrayList<>();
    superClasses = new ArrayList<>();
    fields = new HashMap<>();
    methods = new HashMap<>();
  }

  public void setName(String name) {
    binaryName = name.replace(".", "/");
  }

  @Override
  public HashSet<String> collectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (TypeParam tp : typeParams) {
      for (FieldTypeSignature ts : tp.types) {
        refs.addAll(ts.collectRefClasses());
      }
    }
    for (ClassTypeSignature cs : superClasses) {
      refs.addAll(cs.collectRefClasses());
    }
    for (TypeSignature ts : fields.values()) {
      refs.addAll(ts.collectRefClasses());
    }
    for (MethodTypeSignature ms : methods.values()) {
      refs.addAll(ms.collectRefClasses());
    }
    return refs;
  }

  public ClassTypeSignature toClassTypeSignature() {
    ClassTypeSignature node = new ClassTypeSignature();
    node.binaryName = binaryName;
    return node;
  }
}

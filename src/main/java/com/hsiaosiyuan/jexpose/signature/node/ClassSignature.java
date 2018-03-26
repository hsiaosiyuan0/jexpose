package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;
import com.hsiaosiyuan.jexpose.ClassResolver;

import java.util.*;

public class ClassSignature extends Node {
  @JSONField(serialize = false)
  public String binaryName;

  @JSONField(serialize = false)
  public ArrayList<TypeParam> typeParams;

  @JSONField(serialize = false)
  public ArrayList<ClassTypeSignature> superClasses;

  public boolean isInterface;

  @JSONField(serialize = false)
  public HashMap<String, TypeSignature> fields;

  @JSONField(serialize = false)
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

  @JSONField(serialize = false)
  public ClassTypeSignature getSuperClass() {
    if (superClasses.size() > 0) return superClasses.get(0);
    return null;
  }

  @JSONField(name = "fields")
  public HashMap<String, TypeSignature> getFinalFields() {
    HashMap<String, ClassSignature> classPool = ClassResolver.getClassPool();
    HashMap<String, TypeSignature> fields = new HashMap<>();
    Stack<ClassTypeSignature> superClassesStack = new Stack<>();
    ClassTypeSignature sct = getSuperClass();
    while (sct != null) {
      superClassesStack.push(sct);
      sct = classPool.get(sct.binaryName).getSuperClass();
    }
    while (!superClassesStack.empty()) {
      ClassTypeSignature cs = superClassesStack.pop();
      fields.putAll(classPool.get(cs.binaryName).fields);
    }
    fields.putAll(this.fields);
    HashMap<String, TypeSignature> noStatic = new HashMap<>();
    for (Map.Entry<String, TypeSignature> entry : fields.entrySet()) {
      String key = entry.getKey();
      TypeSignature value = entry.getValue();
      if (!value.isStatic) {
        noStatic.put(key, value);
      }
    }
    return noStatic;
  }

  @JSONField(name = "methods")
  public HashMap<String, MethodTypeSignature> getMethods() {
    HashMap<String, MethodTypeSignature> noInternal = new HashMap<>();
    for (Map.Entry<String, MethodTypeSignature> entry : methods.entrySet()) {
      String key = entry.getKey();
      MethodTypeSignature value = entry.getValue();
      if (key.equals("<init>") || key.equals("<clinit>")) {
        continue;
      }
      noInternal.put(key, value);
    }
    return noInternal;
  }
}

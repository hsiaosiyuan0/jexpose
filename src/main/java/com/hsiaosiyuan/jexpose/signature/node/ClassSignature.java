package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;
import com.hsiaosiyuan.jexpose.ClassResolver;

import java.util.*;
import java.util.stream.Collectors;

public class ClassSignature extends Node {
  @JSONField(serialize = false)
  public String binaryName;

  @JSONField(serialize = false)
  public ArrayList<TypeParam> typeParams;

  @JSONField(serialize = false)
  public ArrayList<TypeParam> appliedTypeParams;

  @JSONField(serialize = false)
  public ArrayList<ClassTypeSignature> superClasses;

  public boolean isInterface;

  public boolean isEnum;

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

  @Override
  public HashSet<String> getDirectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (TypeParam tp : typeParams) {
      for (FieldTypeSignature ts : tp.types) {
        refs.addAll(ts.getDirectRefClasses());
      }
    }
    try {
      for (TypeSignature ts : getFinalFields().values()) {
        refs.addAll(ts.getDirectRefClasses());
      }
    } catch (CloneNotSupportedException ignored) {
    }
    for (MethodTypeSignature ms : methods.values()) {
      refs.addAll(ms.getDirectRefClasses());
    }
    return refs;
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
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

  @JSONField(name = "name")
  public String getName() {
    return binaryName.replace("/", ".");
  }

  @JSONField(name = "fields")
  public HashMap<String, TypeSignature> getFinalFields() throws CloneNotSupportedException {
    HashMap<String, ClassSignature> classPool = ClassResolver.getClassPool();
    HashMap<String, TypeSignature> fields = new HashMap<>();
    ArrayList<ClassTypeSignature> superClasses = new ArrayList<>();
    ClassTypeSignature sct = getSuperClass();
    while (sct != null) {
      superClasses.add(sct);
      sct = classPool.get(sct.binaryName).getSuperClass();
    }

    ArrayList<HashMap<String, TypeSignature>> superFields = new ArrayList<>();
    ArrayList<TypeParam> childTypeParams = typeParams;
    ArrayList<TypeParam> childAppliedTypeParams = typeParams;
    for (ClassTypeSignature sp : superClasses) {
      ArrayList<String> spTypArgNames = sp.getTypeArgNames();
      ArrayList<TypeParam> childTPSMatched = new ArrayList<>();
      for (String sn : spTypArgNames) {
        for (TypeParam ctp : childTypeParams) {
          if (ctp.name.equals(sn)) {
            int idx = getTypeParamIndex(childTypeParams, sn);
            try {
              TypeParam tp = childAppliedTypeParams.get(idx);
              childTPSMatched.add(tp);
            } catch (Exception ignored) {
            }
          }
        }
      }

      ClassSignature spc = ClassResolver.getClassPool().get(sp.binaryName);
      HashMap<String, TypeSignature> appliedFields = spc.applyChildTypeParams(childTPSMatched);
      superFields.add(appliedFields);
      childTypeParams = spc.typeParams;
      childAppliedTypeParams = spc.appliedTypeParams;
    }

    for (int i = superFields.size() - 1; i >= 0; --i) {
      fields.putAll(superFields.get(i));
    }

    fields.putAll(this.fields);
    if (isEnum)
      return (HashMap<String, TypeSignature>) fields.entrySet().stream()
        .filter(item -> !item.getKey().equals("$VALUES"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

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
    if (isEnum) return noInternal;

    for (Map.Entry<String, MethodTypeSignature> entry : methods.entrySet()) {
      String key = entry.getKey();
      MethodTypeSignature value = entry.getValue();
      if (key.startsWith("<init>") || key.startsWith("<clinit>")) {
        continue;
      }
      noInternal.put(key, value);
    }
    return noInternal;
  }

  @JSONField(serialize = false)
  public static int getTypeParamIndex(ArrayList<TypeParam> params, String name) {
    for (int i = 0; i < params.size(); ++i) {
      if (params.get(i).name.equals(name)) return i;
    }
    return -1;
  }

  public HashMap<String, TypeSignature> copyFields() throws CloneNotSupportedException {
    HashMap<String, TypeSignature> ret = new HashMap<>();
    for (Map.Entry<String, TypeSignature> entry : fields.entrySet()) {
      ret.put(entry.getKey(), (TypeSignature) entry.getValue().clone());
    }
    return ret;
  }

  public HashMap<String, TypeSignature> applyChildTypeParams(ArrayList<TypeParam> childTps) throws CloneNotSupportedException {
    appliedTypeParams = new ArrayList<>();
    HashMap<String, TypeSignature> fields = copyFields();
    for (Map.Entry<String, TypeSignature> entry : fields.entrySet()) {
      String key = entry.getKey();
      TypeSignature val = entry.getValue();
      if (!val.isTypeVar()) continue;

      int idx = getTypeParamIndex(typeParams, val.asTypeVar().name);
      TypeParam tp;
      try {
        tp = childTps.get(idx);
      } catch (Exception e) {
        tp = null;
      }
      if (tp != null) {
        val.asTypeVar().name = tp.name;
        continue;
      }

      fields.put(key, typeParams.get(idx).types.get(0));
    }
    appliedTypeParams.addAll(childTps);
    return fields;
  }

  @JSONField(name = "typeParams")
  public ArrayList<TypeParam> getFinalTypeParams() {

    return typeParams;
  }

  @JSONField(serialize = false)
  public String genUniqueMethodName(String name) {
    Set<String> names = methods.keySet();
    for (int i = 0; i < 100; ++i) {
      String n = name + "@override" + i;
      if (!names.contains(n)) return n;
    }
    throw new UnsupportedOperationException("too many override methods");
  }

  public void addMethod(String name, MethodTypeSignature method) {
    if (methods.get(name) == null) {
      methods.put(name, method);
      return;
    }
    method.isOverride = true;
    name = genUniqueMethodName(name);
    methods.put(name, method);
  }
}

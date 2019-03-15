package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ClassTypeSignature extends FieldTypeSignature {
  @JSONField(serialize = false)
  public String binaryName;
  public LinkedList<TypeArg> typeArgs;

  @JSONField(serialize = false)
  public LinkedList<Sub> subTypes;

  public ClassTypeSignature() {
    typeArgs = new LinkedList<>();
    subTypes = new LinkedList<>();
  }

  @JSONField(name = "name")
  public String getName() {
    return binaryName.replace("/", ".");
  }

  public static class Sub extends Node {
    public String name;
    public LinkedList<TypeArg> typeArgs;

    public Sub() {
      typeArgs = new LinkedList<>();
    }

    @Override
    public HashSet<String> collectRefClasses() {
      HashSet<String> refs = new HashSet<>();
      for (TypeArg ta : typeArgs) {
        refs.addAll(ta.collectRefClasses());
      }
      return refs;
    }

    @Override
    public HashSet<String> getDirectRefClasses() {
      HashSet<String> refs = new HashSet<>();
      for (TypeArg ta : typeArgs) {
        refs.addAll(ta.getDirectRefClasses());
      }
      return refs;
    }

    @Override
    protected Node clone() throws CloneNotSupportedException {
      Sub s = new Sub();
      s.name = name;
      for (TypeArg ta : typeArgs) {
        s.typeArgs.add((TypeArg) ta.clone());
      }
      return s;
    }
  }

  @Override
  public HashSet<String> collectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    refs.add(binaryName);
    for (TypeArg ta : typeArgs) {
      refs.addAll(ta.collectRefClasses());
    }
    for (Sub s : subTypes) {
      refs.addAll(s.collectRefClasses());
    }
    return refs;
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    refs.add(binaryName);
    for (TypeArg ta : typeArgs) {
      refs.addAll(ta.getDirectRefClasses());
    }
    for (Sub s : subTypes) {
      refs.addAll(s.getDirectRefClasses());
    }
    return refs;
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    ClassTypeSignature cs = new ClassTypeSignature();
    cs.binaryName = binaryName;
    for (TypeArg ta : typeArgs) {
      cs.typeArgs.add((TypeArg) ta.clone());
    }
    for (Sub s : subTypes) {
      cs.subTypes.add((Sub) s.clone());
    }
    return cs;
  }

  @JSONField(serialize = false)
  public ArrayList<String> getTypeArgNames() {
    ArrayList<String> ret = new ArrayList<>();
    for (TypeArg arg : typeArgs) {
      if (arg.type.isTypeVar()) {
        ret.add(arg.type.asTypeVar().name);
      }
    }
    return ret;
  }

  @Override
  void applyTypeArgs(HashMap<String, TypeArg> args) {
    for (int i = 0; i < typeArgs.size(); ++i) {
      TypeArg a = typeArgs.get(i);
      if (a.type == null) return;
      if (a.type.isTypeVar()) {
        typeArgs.set(i, args.get(a.type.asTypeVar().name));
      } else {
        a.type.applyTypeArgs(args);
      }
    }
  }
}
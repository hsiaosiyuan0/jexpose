package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashSet;
import java.util.LinkedList;

public class ClassTypeSignature extends FieldTypeSignature {
  @JSONField(serialize = false)
  public String binaryName;
  public LinkedList<TypeArg> typeArgs;
  public LinkedList<Sub> subTypes;

  public ClassTypeSignature() {
    typeArgs = new LinkedList<>();
    subTypes = new LinkedList<>();
  }

  @JSONField(name = "name")
  public String getName() {
    return binaryName.replace("/", ".");
  }

  public boolean hasTypeArgs() {
    return typeArgs.size() > 0;
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
}
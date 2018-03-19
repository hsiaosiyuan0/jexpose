package com.hsiaosiyuan.jexpose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Type {
  String name;
  TypeKind kind;

  Type superClazz;
  ArrayList<Type> interfaces;
  HashMap<String, Type> fields;
  HashMap<String, Type> methods;

  // generic
  LinkedList<Type> typeVars;
  LinkedList<Type> typeArgs;

  // method
  Type ret;
  LinkedList<Type> params;

  // array
  Type arrVal;

  private Type() {
  }

  static Type newClazz(String name) {
    Type type = new Type();
    type.kind = TypeKind.Clazz;
    type.name = normalizeName(name);
    type.interfaces = new ArrayList<>();
    type.fields = new HashMap<>();
    type.methods = new HashMap<>();
    type.typeArgs = new LinkedList<>();
    type.typeVars = new LinkedList<>();
    return type;
  }

  static Type newInterface(String name) {
    Type type = newClazz(name);
    type.kind = TypeKind.Itf;
    return type;
  }

  static Type newPrimitive(String name) {
    Type type = new Type();
    type.kind = TypeKind.Primitive;
    type.name = normalizeName(name);
    return type;
  }

  static Type newArray() {
    Type type = new Type();
    type.kind = TypeKind.Array;
    type.name = "java/lang/Array";
    return type;
  }

  static Type newMethod(String name) {
    Type type = new Type();
    type.kind = TypeKind.Method;
    type.name = name;
    type.params = new LinkedList<>();
    return type;
  }

  static Type newTypeVar(String name) {
    Type type = new Type();
    type.kind = TypeKind.TypeVar;
    type.name = name;
    return type;
  }

  static Type newVoidType() {
    Type type = new Type();
    type.name = "java/lang/Void";
    return type;
  }

  static Type newWildcardType() {
    Type type = new Type();
    type.name = "*";
    type.kind = TypeKind.Wildcard;
    return type;
  }

  static String normalizeName(String name) {
    return name.replace(".", "/");
  }

  boolean isClazz() {
    return kind == TypeKind.Clazz;
  }

  boolean isInterface() {
    return kind == TypeKind.Itf;
  }

  boolean isClazzOrInterface() {
    return isClazz() || isInterface();
  }

  boolean isBuiltin() {
    return name.startsWith("java/lang/");
  }

  boolean isPrimitive() {
    return kind == TypeKind.Primitive;
  }

  boolean isArray() {
    return kind == TypeKind.Array;
  }

  boolean isTypeVar() {
    return kind == TypeKind.TypeVar;
  }

  boolean isWildcard() {
    return kind == TypeKind.Wildcard;
  }

  boolean isGeneric() {
    return typeArgs != null;
  }
}
